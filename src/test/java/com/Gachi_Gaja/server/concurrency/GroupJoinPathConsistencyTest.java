package com.Gachi_Gaja.server.concurrency;

import com.Gachi_Gaja.server.exception.AlreadyExistsException;
import com.Gachi_Gaja.server.exception.GroupFullException;
import com.Gachi_Gaja.server.service.GroupService;
import com.Gachi_Gaja.server.service.InviteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 그룹 참가 진입점이 두 개라서 생긴 우회 경로의 회귀 테스트.
 *
 * InviteService.joinGroup(POST /api/invites/{groupId}) 만 고쳤을 때,
 * GroupService.addMemberToGroup(POST /api/groups/{groupId}/members) 은
 * 정원 체크도 비관적 락도 없이 그대로 save 하고 있었다.
 * 즉 한쪽을 아무리 잘 고쳐도 다른 쪽으로 들어오면 정원이 무너졌다.
 *
 * 그래서 이 테스트는 "락이 동작하는가"가 아니라
 * **모든 진입점이 같은 규칙을 통과하는가**를 확인한다.
 * 참가 경로를 새로 추가한다면 아래 목록에 함께 추가할 것.
 */
@EnabledIf("com.Gachi_Gaja.server.concurrency.MySqlAvailable#isReachable")
class GroupJoinPathConsistencyTest extends ConcurrencyTestSupport {

    private static final int CAPACITY = 5;

    @Autowired InviteService inviteService;
    @Autowired GroupService groupService;

    /** 현재 존재하는 모든 "그룹 참가" 진입점. (userId, groupId) 를 받는 형태로 통일해 둔다. */
    private List<JoinPath> joinPaths() {
        return List.of(
                // 중복 가입 시 응답은 경로마다 다르다(명세 기준). 정원 규칙만 공통이다.
                new JoinPath("InviteService.joinGroup", true,
                        (userId, groupId) -> inviteService.joinGroup(userId, groupId)),
                // MemberAppendRequest 명세: "이미 추가된 User인 경우 ... 마찬가지로 200"
                new JoinPath("GroupService.addMemberToGroup", false,
                        (userId, groupId) -> groupService.addMemberToGroup(groupId, userId))
        );
    }

    @Test
    @DisplayName("모든 참가 경로가 순차 가입에서 정원을 지켜야 한다")
    void everyJoinPath_shouldRejectBeyondCapacity() {
        for (JoinPath path : joinPaths()) {
            setUpFixture(); // 경로마다 그룹/유저를 새로 만든다

            for (int i = 0; i < CAPACITY; i++) {
                path.join(userIds.get(i), groupId);
            }
            assertThat(currentMemberCount()).isEqualTo(CAPACITY);

            assertThatThrownBy(() -> path.join(userIds.get(CAPACITY), groupId))
                    .as("%s 로 6번째가 들어갔다 — 이 경로에 정원 체크가 없다", path.name())
                    .isInstanceOf(GroupFullException.class);

            assertThat(currentMemberCount())
                    .as("%s 경로에서 정원 초과", path.name())
                    .isEqualTo(CAPACITY);
        }
    }

    @Test
    @DisplayName("모든 참가 경로가 중복 가입으로 멤버를 늘리지 않아야 한다 (응답은 명세별로 다름)")
    void everyJoinPath_shouldNotDuplicateMember() {
        for (JoinPath path : joinPaths()) {
            setUpFixture();

            path.join(userIds.get(0), groupId);

            // 두 번째 요청의 "응답"은 경로마다 다르지만, "결과"는 같아야 한다 (멤버 1명).
            if (path.throwsOnDuplicate()) {
                assertThatThrownBy(() -> path.join(userIds.get(0), groupId))
                        .as("%s 는 중복 가입을 409 로 알려야 한다", path.name())
                        .isInstanceOf(AlreadyExistsException.class);
            } else {
                // 명세상 멱등: 예외 없이 조용히 200. 여기서 예외가 나면 명세 위반이다.
                path.join(userIds.get(0), groupId);
            }

            assertThat(currentMemberCount())
                    .as("%s 로 같은 사용자가 두 번 들어갔다", path.name())
                    .isEqualTo(1);
        }
    }

    @Test
    @DisplayName("두 경로로 동시에 들어와도 정원 5명을 넘지 않아야 한다")
    void mixedJoinPaths_concurrent_shouldNotExceedCapacity() throws Exception {
        // 같은 그룹에 서로 다른 두 진입점으로 동시에 몰아넣는다.
        // 한쪽만 락을 잡으면 다른 쪽이 그 사이를 뚫고 들어간다.
        List<JoinPath> paths = joinPaths();
        List<UUID> participants = userIds.subList(0, 10);

        int n = participants.size();
        ExecutorService pool = Executors.newFixedThreadPool(n);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(n);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();
        ConcurrentLinkedQueue<String> others = new ConcurrentLinkedQueue<>();

        try {
            for (int i = 0; i < n; i++) {
                UUID userId = participants.get(i);
                JoinPath path = paths.get(i % paths.size()); // 두 경로를 번갈아 섞는다
                pool.submit(() -> {
                    try {
                        startGate.await();
                        path.join(userId, groupId);
                        success.incrementAndGet();
                    } catch (GroupFullException | AlreadyExistsException e) {
                        rejected.incrementAndGet();
                    } catch (Exception e) {
                        others.add(path.name() + " → " + e.getClass().getSimpleName() + ": " + e.getMessage());
                    } finally {
                        doneGate.countDown();
                    }
                });
            }
            startGate.countDown();
            assertThat(doneGate.await(60, TimeUnit.SECONDS))
                    .as("60초 내에 끝나지 않았다 — 락 대기/데드락 의심").isTrue();
        } finally {
            pool.shutdownNow();
        }

        System.out.printf("[혼합경로] 성공=%d 거절=%d 기타예외=%d 최종인원=%d%n",
                success.get(), rejected.get(), others.size(), currentMemberCount());
        others.forEach(e -> System.out.println("  기타예외: " + e));

        assertThat(currentMemberCount())
                .as("두 경로가 섞이자 정원이 초과됐다 — 한쪽 진입점이 락/정원 체크를 건너뛴다")
                .isEqualTo(CAPACITY);
        assertThat(success.get()).isEqualTo(CAPACITY);
    }

    /**
     *  throwsOnDuplicate 중복 가입 시 예외를 던지는가(명세상 409). false 면 조용히 성공(명세상 200).
     */
    private record JoinPath(String name, boolean throwsOnDuplicate, BiConsumer<UUID, UUID> action) {
        void join(UUID userId, UUID groupId) {
            action.accept(userId, groupId);
        }
    }
}
