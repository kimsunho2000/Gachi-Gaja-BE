package com.Gachi_Gaja.server.concurrency;

import com.Gachi_Gaja.server.domain.Group;
import com.Gachi_Gaja.server.domain.Member;
import com.Gachi_Gaja.server.domain.User;
import com.Gachi_Gaja.server.repository.CandidatePlanRepository;
import com.Gachi_Gaja.server.repository.GroupRepository;
import com.Gachi_Gaja.server.repository.MemberRepository;
import com.Gachi_Gaja.server.repository.MemberVoteRepository;
import com.Gachi_Gaja.server.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 동시성 재현 테스트 공통 픽스처.
 *
 * 주의: 이 테스트들은 반드시 MySQL(InnoDB) 위에서 돌아야 한다.
 * SELECT ... FOR UPDATE 의 대기 동작과 REPEATABLE READ 스냅샷 시점은
 * DB 구현에 종속적이라 H2 인메모리로는 재현되지 않는다.
 */
@SpringBootTest
@EnabledIf("com.Gachi_Gaja.server.concurrency.MySqlAvailable#isReachable")
@ActiveProfiles("test")
public abstract class ConcurrencyTestSupport {

    @Autowired protected GroupRepository groupRepository;
    @Autowired protected MemberRepository memberRepository;
    @Autowired protected UserRepository userRepository;
    @Autowired protected CandidatePlanRepository candidatePlanRepository;
    @Autowired protected MemberVoteRepository memberVoteRepository;
    @Autowired protected TransactionTemplate txTemplate;

    protected UUID groupId;
    protected List<UUID> userIds;

    @BeforeEach
    void setUpFixture() {
        // 테스트 간 격리: 자식 → 부모 순으로 정리 (FK 참조 역순)
        memberVoteRepository.deleteAllInBatch();
        candidatePlanRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        groupRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        Group group = Group.builder()
                .title("동시성 테스트 모임")
                .region("대전")
                .startingPoint("대전역")
                .endingPoint("대전역")
                .transportation("대중교통")
                .period("1박 2일")
                .budget(200_000)
                .startingDay(LocalDate.now().plusDays(7))
                .endingDay(LocalDate.now().plusDays(8))
                .requirementDeadline(LocalDate.now().plusDays(3)) // 마감일 체크를 통과해야 인원 체크까지 도달한다
                .voteDeadline(null)
                .callCnt(3)
                .build();
        groupId = groupRepository.save(group).getGroupId();

        userIds = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            User user = User.builder()
                    .email("tester" + i + "@gachi.test")
                    .password("pw")
                    .nickname("tester" + i)
                    .build();
            userIds.add(userRepository.save(user).getUserId());
        }
    }

    /** 그룹에 멤버를 미리 채워 넣는다(테스트 사전 조건 세팅용). */
    protected void seedMembers(int count) {
        Group group = groupRepository.findById(groupId).orElseThrow();
        for (int i = 0; i < count; i++) {
            User user = userRepository.findById(userIds.get(i)).orElseThrow();
            memberRepository.save(Member.builder()
                    .group(group)
                    .user(user)
                    .isLeader(i == 0)
                    .build());
        }
    }

    protected long currentMemberCount() {
        return memberRepository.findByGroup_GroupId(groupId).size();
    }
}
