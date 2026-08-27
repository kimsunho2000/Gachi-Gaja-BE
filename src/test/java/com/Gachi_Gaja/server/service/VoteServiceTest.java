package com.Gachi_Gaja.server.service;

import com.Gachi_Gaja.server.domain.CandidatePlan;
import com.Gachi_Gaja.server.domain.Group;
import com.Gachi_Gaja.server.domain.Member;
import com.Gachi_Gaja.server.domain.MemberVote;
import com.Gachi_Gaja.server.domain.User;
import com.Gachi_Gaja.server.dto.request.VoteRequestDTO;
import com.Gachi_Gaja.server.exception.NotFoundException;
import com.Gachi_Gaja.server.repository.CandidatePlanRepository;
import com.Gachi_Gaja.server.repository.GroupRepository;
import com.Gachi_Gaja.server.repository.MemberRepository;
import com.Gachi_Gaja.server.repository.MemberVoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

/**
 * VoteService 단위 테스트
 * 검증 목표:
 * 1. 첫 투표 시 MemberVote 신규 생성 및 voteCount +1
 * 2. 투표 변경 시 기존 후보 voteCount -1, 신규 후보 voteCount +1
 * 3. 리더 투표 시 isVoted 플래그 변경
 * 4. Lost Update 방지 핵심 로직 (순차 처리 시뮬레이션)
 * 5. 존재하지 않는 그룹/멤버/후보 접근 시 NotFoundException
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VoteService 단위 테스트")
class VoteServiceTest {

    @Mock private CandidatePlanRepository candidatePlanRepository;
    @Mock private GroupRepository groupRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private MemberVoteRepository memberVoteRepository;

    @InjectMocks
    private VoteService voteService;

    private UUID userId;
    private UUID groupId;
    private UUID planId;

    private User user;
    private Group group;
    private Member regularMember;
    private Member leaderMember;
    private CandidatePlan candidatePlan;

    @BeforeEach
    void setUp() throws Exception {
        userId  = UUID.randomUUID();
        groupId = UUID.randomUUID();
        planId  = UUID.randomUUID();

        user = User.builder()
                .email("voter@example.com").password("pw").nickname("투표자").build();
        setPrivateField(user, "userId", userId);

        group = Group.builder()
                .title("투표 테스트 그룹").region("서울").startingPoint("강남").endingPoint("홍대")
                .transportation("지하철").period("1박 2일").budget(100000)
                .startingDay(LocalDate.now().plusDays(10))
                .endingDay(LocalDate.now().plusDays(11))
                .requirementDeadline(LocalDate.now().plusDays(5))
                .voteDeadline(null).callCnt(3).build();
        setPrivateField(group, "groupId", groupId);

        regularMember = Member.builder().user(user).group(group).isLeader(false).build();
        leaderMember  = Member.builder().user(user).group(group).isLeader(true).build();

        candidatePlan = CandidatePlan.builder()
                .group(group).planContent("후보 일정 A").voteCount(0).isVoted(false).build();
        setPrivateField(candidatePlan, "candidatePlanId", planId);
    }

    // ─────────────────────────────────────────────────────────────
    // 1. 첫 투표 (신규 MemberVote 생성)
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("첫 투표 (createVote - 신규)")
    class FirstVote {

        @Test
        @DisplayName("정상: 첫 투표 시 MemberVote가 저장되고 voteCount가 1 증가한다")
        void success_firstVote() {
            given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
            given(memberRepository.findByUser_UserIdAndGroup_GroupId(userId, groupId))
                    .willReturn(Optional.of(regularMember));
            given(candidatePlanRepository.findById(planId))
                    .willReturn(Optional.of(candidatePlan));
            given(memberVoteRepository.findByUser_UserIdAndGroup_GroupId(userId, groupId))
                    .willReturn(Optional.empty());

            voteService.createVote(groupId, userId, new VoteRequestDTO(planId.toString()));

            then(memberVoteRepository).should(times(1)).save(any(MemberVote.class));
            assertThat(candidatePlan.getVoteCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("정상: 일반 멤버의 첫 투표에서 isVoted 플래그는 변경되지 않는다")
        void success_firstVote_isVotedNotChangedForRegularMember() {
            given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
            given(memberRepository.findByUser_UserIdAndGroup_GroupId(userId, groupId))
                    .willReturn(Optional.of(regularMember));
            given(candidatePlanRepository.findById(planId))
                    .willReturn(Optional.of(candidatePlan));
            given(memberVoteRepository.findByUser_UserIdAndGroup_GroupId(userId, groupId))
                    .willReturn(Optional.empty());

            voteService.createVote(groupId, userId, new VoteRequestDTO(planId.toString()));

            assertThat(candidatePlan.isVoted()).isFalse();
        }

        @Test
        @DisplayName("정상: 리더의 첫 투표 시 isVoted 플래그가 true로 설정된다")
        void success_firstVote_isVotedTrueForLeader() {
            given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
            given(memberRepository.findByUser_UserIdAndGroup_GroupId(userId, groupId))
                    .willReturn(Optional.of(leaderMember));
            given(candidatePlanRepository.findById(planId))
                    .willReturn(Optional.of(candidatePlan));
            given(memberVoteRepository.findByUser_UserIdAndGroup_GroupId(userId, groupId))
                    .willReturn(Optional.empty());

            voteService.createVote(groupId, userId, new VoteRequestDTO(planId.toString()));

            assertThat(candidatePlan.isVoted()).isTrue();
            assertThat(candidatePlan.getVoteCount()).isEqualTo(1);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 2. 투표 변경 (기존 MemberVote 수정 — Lost Update 핵심 시나리오)
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("투표 변경 (createVote - 기존 투표 교체)")
    class ChangeVote {

        @Test
        @DisplayName("정상: 투표 변경 시 기존 후보 -1, 신규 후보 +1이 정확히 반영된다 (Lost Update 방지 핵심)")
        void success_changeVote_correctVoteCountUpdate() throws Exception {
            UUID oldPlanId = UUID.randomUUID();
            CandidatePlan oldPlan = CandidatePlan.builder()
                    .group(group).planContent("후보 B").voteCount(5).isVoted(false).build();
            setPrivateField(oldPlan, "candidatePlanId", oldPlanId);

            candidatePlan.updateVoteCount(3);

            MemberVote existingVote = MemberVote.builder()
                    .user(user).group(group).candidatePlan(oldPlan).build();

            given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
            given(memberRepository.findByUser_UserIdAndGroup_GroupId(userId, groupId))
                    .willReturn(Optional.of(regularMember));
            given(candidatePlanRepository.findById(planId))
                    .willReturn(Optional.of(candidatePlan));
            given(memberVoteRepository.findByUser_UserIdAndGroup_GroupId(userId, groupId))
                    .willReturn(Optional.of(existingVote));

            voteService.createVote(groupId, userId, new VoteRequestDTO(planId.toString()));

            // 기존 후보 5 → 4
            assertThat(oldPlan.getVoteCount()).isEqualTo(4);
            // 신규 후보 3 → 4
            assertThat(candidatePlan.getVoteCount()).isEqualTo(4);
            then(memberVoteRepository).should(times(1)).save(any(MemberVote.class));
        }

        @Test
        @DisplayName("정상: 리더가 투표를 변경하면 기존 후보 isVoted=false, 신규 후보 isVoted=true")
        void success_changeVote_leaderIsVotedTransfer() throws Exception {
            UUID oldPlanId = UUID.randomUUID();
            CandidatePlan oldPlan = CandidatePlan.builder()
                    .group(group).planContent("기존 리더 선택").voteCount(3).isVoted(true).build();
            setPrivateField(oldPlan, "candidatePlanId", oldPlanId);

            MemberVote existingVote = MemberVote.builder()
                    .user(user).group(group).candidatePlan(oldPlan).build();

            given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
            given(memberRepository.findByUser_UserIdAndGroup_GroupId(userId, groupId))
                    .willReturn(Optional.of(leaderMember));
            given(candidatePlanRepository.findById(planId))
                    .willReturn(Optional.of(candidatePlan));
            given(memberVoteRepository.findByUser_UserIdAndGroup_GroupId(userId, groupId))
                    .willReturn(Optional.of(existingVote));

            voteService.createVote(groupId, userId, new VoteRequestDTO(planId.toString()));

            assertThat(oldPlan.isVoted()).isFalse();
            assertThat(candidatePlan.isVoted()).isTrue();
        }

        @Test
        @DisplayName("정상: 투표 변경 시 신규/기존 후보 모두 candidatePlanRepository.save가 호출된다")
        void success_changeVote_saveCalledForBothPlans() throws Exception {
            UUID oldPlanId = UUID.randomUUID();
            CandidatePlan oldPlan = CandidatePlan.builder()
                    .group(group).planContent("후보 C").voteCount(2).isVoted(false).build();
            setPrivateField(oldPlan, "candidatePlanId", oldPlanId);

            MemberVote existingVote = MemberVote.builder()
                    .user(user).group(group).candidatePlan(oldPlan).build();

            given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
            given(memberRepository.findByUser_UserIdAndGroup_GroupId(userId, groupId))
                    .willReturn(Optional.of(regularMember));
            given(candidatePlanRepository.findById(planId))
                    .willReturn(Optional.of(candidatePlan));
            given(memberVoteRepository.findByUser_UserIdAndGroup_GroupId(userId, groupId))
                    .willReturn(Optional.of(existingVote));

            voteService.createVote(groupId, userId, new VoteRequestDTO(planId.toString()));

            // 기존 후보 save + 신규 후보 save = 총 2번
            then(candidatePlanRepository).should(times(2)).save(any(CandidatePlan.class));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 3. 동시 투표 순차 처리 시뮬레이션 (Lost Update 방지 검증)
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("동시 투표 순차 처리 시뮬레이션")
    class ConcurrentVoteSimulation {

        /**
         * 비관적 락 환경에서 동시 요청은 순차 처리됩니다.
         * 3명이 순차 투표했을 때 voteCount = 3인지 검증합니다.
         */
        @Test
        @DisplayName("시뮬레이션: 3명이 순차 투표 시 voteCount = 3 (Lost Update 없이 원자성 보장)")
        void simulation_threeUsersVote_voteCountIsThree() throws Exception {
            UUID userIdA = UUID.randomUUID();
            UUID userIdB = UUID.randomUUID();
            UUID userIdC = UUID.randomUUID();

            User userA = User.builder().email("a@test.com").password("pw").nickname("A").build();
            User userB = User.builder().email("b@test.com").password("pw").nickname("B").build();
            User userC = User.builder().email("c@test.com").password("pw").nickname("C").build();
            setPrivateField(userA, "userId", userIdA);
            setPrivateField(userB, "userId", userIdB);
            setPrivateField(userC, "userId", userIdC);

            Member memberA = Member.builder().user(userA).group(group).isLeader(false).build();
            Member memberB = Member.builder().user(userB).group(group).isLeader(false).build();
            Member memberC = Member.builder().user(userC).group(group).isLeader(false).build();

            CandidatePlan sharedPlan = CandidatePlan.builder()
                    .group(group).planContent("공유 후보").voteCount(0).isVoted(false).build();
            UUID sharedPlanId = UUID.randomUUID();
            setPrivateField(sharedPlan, "candidatePlanId", sharedPlanId);

            given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
            given(candidatePlanRepository.findById(sharedPlanId)).willReturn(Optional.of(sharedPlan));
            given(memberVoteRepository.findByUser_UserIdAndGroup_GroupId(any(), eq(groupId)))
                    .willReturn(Optional.empty());
            given(memberRepository.findByUser_UserIdAndGroup_GroupId(userIdA, groupId))
                    .willReturn(Optional.of(memberA));
            given(memberRepository.findByUser_UserIdAndGroup_GroupId(userIdB, groupId))
                    .willReturn(Optional.of(memberB));
            given(memberRepository.findByUser_UserIdAndGroup_GroupId(userIdC, groupId))
                    .willReturn(Optional.of(memberC));

            // 비관적 락으로 인해 순차 처리됨
            voteService.createVote(groupId, userIdA, new VoteRequestDTO(sharedPlanId.toString()));
            voteService.createVote(groupId, userIdB, new VoteRequestDTO(sharedPlanId.toString()));
            voteService.createVote(groupId, userIdC, new VoteRequestDTO(sharedPlanId.toString()));

            // Lost Update 없이 voteCount = 3
            assertThat(sharedPlan.getVoteCount()).isEqualTo(3);
            then(memberVoteRepository).should(times(3)).save(any(MemberVote.class));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 4. 예외 처리
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("예외 처리")
    class ExceptionHandling {

        @Test
        @DisplayName("실패: 존재하지 않는 groupId로 투표 시 NotFoundException 발생")
        void fail_groupNotFound() {
            given(groupRepository.findById(groupId)).willReturn(Optional.empty());

            assertThatThrownBy(() ->
                    voteService.createVote(groupId, userId, new VoteRequestDTO(planId.toString())))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Group 없음");
        }

        @Test
        @DisplayName("실패: 그룹에 속하지 않은 사용자가 투표 시 NotFoundException 발생")
        void fail_memberNotFound() {
            given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
            given(memberRepository.findByUser_UserIdAndGroup_GroupId(userId, groupId))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() ->
                    voteService.createVote(groupId, userId, new VoteRequestDTO(planId.toString())))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Member 없음");
        }

        @Test
        @DisplayName("실패: 존재하지 않는 candidatePlanId로 투표 시 NotFoundException 발생")
        void fail_candidatePlanNotFound() {
            given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
            given(memberRepository.findByUser_UserIdAndGroup_GroupId(userId, groupId))
                    .willReturn(Optional.of(regularMember));
            given(candidatePlanRepository.findById(planId)).willReturn(Optional.empty());

            assertThatThrownBy(() ->
                    voteService.createVote(groupId, userId, new VoteRequestDTO(planId.toString())))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("후보 없음");
        }

        @Test
        @DisplayName("실패: 잘못된 UUID 형식의 candidatePlanId로 투표 시 IllegalArgumentException 발생")
        void fail_invalidUuidFormat() {
            // UUID.fromString()이 제일 먼저 호출되어 바로 예외 발생
            assertThatThrownBy(() ->
                    voteService.createVote(groupId, userId, new VoteRequestDTO("not-a-valid-uuid")))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ── 유틸 ────────────────────────────────────────────────────
    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
