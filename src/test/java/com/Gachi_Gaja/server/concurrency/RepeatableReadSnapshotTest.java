package com.Gachi_Gaja.server.concurrency;

import com.Gachi_Gaja.server.domain.Group;
import com.Gachi_Gaja.server.domain.Member;
import com.Gachi_Gaja.server.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 3층 — "비관적 락을 걸었는데도 정원이 초과되는" 현상의 근본 원인.
 *
 * MySQL InnoDB 의 기본 격리 수준은 REPEATABLE READ 이고,
 * 일반 SELECT 가 바라보는 스냅샷(read view)은 "트랜잭션 내 첫 일반 읽기" 시점에 고정된다.
 * 반면 SELECT ... FOR UPDATE 는 잠금 읽기라 항상 최신 커밋본을 읽는다.
 *
 * InviteService.joinGroup 의 현재 문장 순서는 다음과 같다.
 *      (1) userRepository.findById(...)           ← 일반 읽기. 여기서 스냅샷이 고정된다.
 *      (2) findByIdWithPessimisticLock(...)       ← 잠금 획득. 앞선 트랜잭션의 커밋을 기다린다.
 *      (3) memberRepository.countByGroup(...)     ← 일반 읽기. (1) 시점의 낡은 스냅샷을 본다.
 *
 * 즉 (2)에서 락을 기다리는 동안 다른 트랜잭션이 INSERT 후 커밋해도,
 * (3)의 인원 수에는 그 INSERT 가 보이지 않는다. 락은 걸렸지만 세어 본 값이 낡은 것이다.
 *
 * 아래 두 테스트는 (1)과 (2)의 순서만 바꿔서 결과가 달라지는 것을 보인다.
 */
@org.junit.jupiter.api.condition.EnabledIf("com.Gachi_Gaja.server.concurrency.MySqlAvailable#isReachable")
class RepeatableReadSnapshotTest extends ConcurrencyTestSupport {

    @PersistenceContext EntityManager em;

    /** 관찰자 트랜잭션이 락을 기다리는 동안, 경쟁 트랜잭션이 멤버 1명을 INSERT 하고 커밋한다. */
    private static final int SEEDED = 4;

    @Test
    @DisplayName("[3층] 일반 읽기를 먼저 하면 — 락을 잡고도 낡은 인원 수를 본다 (현재 코드 순서)")
    void snapshotOpenedBeforeLock_seesStaleCount() throws Exception {
        long observed = runRaceAndObserveCount(/* lockFirst = */ false);

        System.out.printf("[3층-현재순서] 락 획득 후 센 인원=%d, 실제 DB 인원=%d%n",
                observed, currentMemberCount());

        assertThat(currentMemberCount())
                .as("경쟁 트랜잭션의 INSERT 는 분명히 커밋되었다")
                .isEqualTo(SEEDED + 1);

        assertThat(observed)
                .as("락을 잡고 세었는데도 커밋된 INSERT 가 보이지 않는다면 스냅샷 문제가 재현된 것이다")
                .isEqualTo(SEEDED);
    }

    @Test
    @DisplayName("[3층] 잠금 읽기를 먼저 하면 — 최신 인원 수를 본다 (수정안)")
    void lockAcquiredFirst_seesFreshCount() throws Exception {
        long observed = runRaceAndObserveCount(/* lockFirst = */ true);

        System.out.printf("[3층-수정안] 락 획득 후 센 인원=%d, 실제 DB 인원=%d%n",
                observed, currentMemberCount());

        assertThat(observed)
                .as("락을 먼저 잡으면 read view 가 그 이후에 열리므로 최신 커밋이 보여야 한다")
                .isEqualTo(SEEDED + 1);
    }

    // ------------------------------------------------------------------
    // 두 트랜잭션을 래치로 정확히 교차시킨다. 타이밍에 의존하지 않는 결정적 재현이다.
    //
    //   관찰자                                   경쟁자
    //   ----------------------------------------------------------------
    //   (lockFirst=false 일 때) 일반 읽기
    //   ── afterFirstRead ──────────────────▶
    //                                          FOR UPDATE 로 락 획득
    //   ◀───────────────── rivalHoldsLock ──
    //   FOR UPDATE  (여기서 블로킹)
    //                                          INSERT + COMMIT (락 해제)
    //   락 획득 → COUNT 실행
    // ------------------------------------------------------------------
    private long runRaceAndObserveCount(boolean lockFirst) throws Exception {
        seedMembers(SEEDED);
        UUID observerUserId = userIds.get(10);
        UUID rivalUserId = userIds.get(11);

        CountDownLatch afterFirstRead = new CountDownLatch(1);
        CountDownLatch rivalHoldsLock = new CountDownLatch(1);
        CountDownLatch rivalCommitted = new CountDownLatch(1);

        AtomicLong observedCount = new AtomicLong(-1);
        AtomicReference<Throwable> failure = new AtomicReference<>();

        // 각 스레드가 자기 트랜잭션을 갖도록 REQUIRES_NEW 로 분리한다.
        TransactionTemplate newTx = new TransactionTemplate(txTemplate.getTransactionManager());
        newTx.setPropagationBehavior(Propagation.REQUIRES_NEW.value());

        Thread observer = new Thread(() -> newTx.executeWithoutResult(status -> {
            try {
                if (!lockFirst) {
                    // 현재 코드 순서: 락보다 먼저 일반 읽기를 한다 → 여기서 read view 가 고정된다.
                    em.find(User.class, observerUserId);
                }
                afterFirstRead.countDown();
                rivalHoldsLock.await(10, TimeUnit.SECONDS);

                // 두 경우 모두 여기서 블로킹된다. 차이는 "이 시점 이전에 read view 를 열었는가" 뿐이다.
                lockGroup();
                rivalCommitted.await(10, TimeUnit.SECONDS);
                observedCount.set(countMembers());  // 일반 읽기 — 어떤 스냅샷을 볼 것인가?
            } catch (Throwable t) {
                failure.compareAndSet(null, t);
            }
        }), "observer");

        Thread rival = new Thread(() -> newTx.executeWithoutResult(status -> {
            try {
                afterFirstRead.await(10, TimeUnit.SECONDS);
                lockGroup();
                rivalHoldsLock.countDown();

                Group group = em.find(Group.class, groupId);
                User user = em.find(User.class, rivalUserId);
                em.persist(Member.builder().group(group).user(user).isLeader(false).build());
                em.flush(); // 커밋은 트랜잭션 종료 시점
            } catch (Throwable t) {
                failure.compareAndSet(null, t);
            }
        }), "rival");

        observer.start();
        rival.start();

        rival.join(30_000);          // 경쟁자 트랜잭션 커밋 완료
        rivalCommitted.countDown();  // 관찰자에게 "이제 세어 보라"고 알림
        observer.join(30_000);

        if (failure.get() != null) {
            throw new IllegalStateException("재현 중 예외 발생", failure.get());
        }
        return observedCount.get();
    }

    private void lockGroup() {
        em.createQuery("select g from Group g where g.groupId = :id", Group.class)
                .setParameter("id", groupId)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getSingleResult();
    }

    private long countMembers() {
        return em.createQuery(
                        "select count(m) from Member m where m.group.groupId = :gid", Long.class)
                .setParameter("gid", groupId)
                .getSingleResult();
    }
}
