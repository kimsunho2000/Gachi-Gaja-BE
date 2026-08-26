package com.Gachi_Gaja.server.concurrency;

import com.Gachi_Gaja.server.exception.GroupFullException;
import com.Gachi_Gaja.server.service.InviteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * "정원 5명 그룹에 6명이 들어왔다" 트러블슈팅의 재현 테스트.
 *
 * 원인이 한 겹이 아니었으므로, 층을 나눠 각각 독립적으로 재현한다.
 *   1층: 부등호 오류(off-by-one)  → 동시성과 무관. 단일 스레드로 재현된다.
 *   2층: check-then-act 경합      → 동시 요청으로만 재현된다.
 */
@org.junit.jupiter.api.condition.EnabledIf("com.Gachi_Gaja.server.concurrency.MySqlAvailable#isReachable")
class GroupCapacityConcurrencyTest extends ConcurrencyTestSupport {

    /** InviteService.MAX_GROUP_MEMBERS 와 동일한 값. 해당 상수가 package-private 이라 여기서 다시 선언한다. */
    private static final int CAPACITY = 5;

    @Autowired InviteService inviteService;

    // ------------------------------------------------------------------
    // 1층 — off-by-one. 동시성이 전혀 개입하지 않는 순차 시나리오다.
    //   과거 코드: if (count >  MAX) throw ...  → 정원이 5일 때 5 > 5 == false 라 6번째가 통과
    //   현재 코드: if (count >= MAX) throw ...
    // ------------------------------------------------------------------
    @Test
    @DisplayName("[1층] 순차 가입만으로도 정원을 넘지 않아야 한다 (부등호 회귀 테스트)")
    void sequentialJoin_shouldRejectBeyondCapacity() {
        for (int i = 0; i < CAPACITY; i++) {
            inviteService.joinGroup(userIds.get(i), groupId);
        }
        assertThat(currentMemberCount()).isEqualTo(CAPACITY);

        // 6번째 가입 시도 — 여기서 통과하면 off-by-one 이 살아있는 것이다.
        assertThatThrownBy(() -> inviteService.joinGroup(userIds.get(CAPACITY), groupId))
                .isInstanceOf(GroupFullException.class);

        assertThat(currentMemberCount())
                .as("순차 가입에서 정원 초과 — 인원 체크 부등호(off-by-one)를 확인하라")
                .isEqualTo(CAPACITY);
    }

    // ------------------------------------------------------------------
    // 2층 — 동시 요청. check(countByGroup) 와 act(save) 사이의 경합.
    // ------------------------------------------------------------------
    @Test
    @DisplayName("[2층] 빈 그룹에 10명이 동시 가입해도 정원 5명을 넘지 않아야 한다")
    void concurrentJoin_onEmptyGroup_shouldNotExceedCapacity() throws Exception {
        Result result = joinConcurrently(userIds.subList(0, 10));

        System.out.printf("[2층] 성공=%d 정원초과예외=%d 기타예외=%d 최종인원=%d%n",
                result.success, result.groupFull, result.others.size(), currentMemberCount());
        result.others.forEach(e -> System.out.println("  기타예외: " + e));

        assertThat(currentMemberCount())
                .as("동시 가입으로 정원 초과 — check-then-act 구간이 보호되지 않았다")
                .isEqualTo(CAPACITY);
        assertThat(result.success).isEqualTo(CAPACITY);
    }

    @Test
    @DisplayName("[2층] 정원 직전(4명)에서 6명이 동시 가입해도 5명을 넘지 않아야 한다")
    void concurrentJoin_atCapacityBoundary_shouldNotExceedCapacity() throws Exception {
        seedMembers(CAPACITY - 1); // 4명 채워둠 → 남은 자리는 정확히 1개
        assertThat(currentMemberCount()).isEqualTo(CAPACITY - 1);

        // 이미 가입한 0~3번을 제외한 사용자들로 마지막 한 자리를 두고 경합시킨다.
        Result result = joinConcurrently(userIds.subList(CAPACITY - 1, CAPACITY + 5));

        System.out.printf("[2층-경계] 성공=%d 정원초과예외=%d 기타예외=%d 최종인원=%d%n",
                result.success, result.groupFull, result.others.size(), currentMemberCount());
        result.others.forEach(e -> System.out.println("  기타예외: " + e));

        assertThat(result.success)
                .as("남은 자리는 1개인데 여러 명이 성공했다 — 락이 경합을 막지 못했다")
                .isEqualTo(1);
        assertThat(currentMemberCount()).isEqualTo(CAPACITY);
    }

    // ------------------------------------------------------------------
    // 여러 스레드가 최대한 같은 순간에 joinGroup 에 진입하도록 강제한다.
    // startGate 로 모든 스레드를 묶어 두었다가 한 번에 풀어야
    // "먼저 도착한 트랜잭션이 이미 커밋을 끝낸" 우연한 직렬 실행을 피할 수 있다.
    // ------------------------------------------------------------------
    private Result joinConcurrently(List<java.util.UUID> participants) throws Exception {
        int n = participants.size();
        ExecutorService pool = Executors.newFixedThreadPool(n);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(n);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger groupFull = new AtomicInteger();
        ConcurrentLinkedQueue<String> others = new ConcurrentLinkedQueue<>();

        try {
            for (java.util.UUID userId : participants) {
                pool.submit(() -> {
                    try {
                        startGate.await();
                        inviteService.joinGroup(userId, groupId);
                        success.incrementAndGet();
                    } catch (GroupFullException e) {
                        groupFull.incrementAndGet();
                    } catch (Exception e) {
                        // 락 타임아웃, 데드락, UNIQUE 제약 위반 등은 여기로 모인다.
                        others.add(e.getClass().getSimpleName() + ": " + e.getMessage());
                    } finally {
                        doneGate.countDown();
                    }
                });
            }
            startGate.countDown();
            boolean finished = doneGate.await(60, TimeUnit.SECONDS);
            assertThat(finished).as("60초 내에 모든 스레드가 끝나지 않았다 — 락 대기/데드락 의심").isTrue();
        } finally {
            pool.shutdownNow();
        }

        return new Result(success.get(), groupFull.get(), List.copyOf(others));
    }

    private record Result(int success, int groupFull, List<String> others) {}
}
