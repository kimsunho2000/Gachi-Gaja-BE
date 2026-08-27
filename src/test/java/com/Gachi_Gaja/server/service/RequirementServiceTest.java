package com.Gachi_Gaja.server.service;

import com.Gachi_Gaja.server.domain.Group;
import com.Gachi_Gaja.server.domain.Member;
import com.Gachi_Gaja.server.domain.Requirement;
import com.Gachi_Gaja.server.domain.User;
import com.Gachi_Gaja.server.dto.request.RequirementRequestDTO;
import com.Gachi_Gaja.server.dto.response.RequirementResponseDTO;
import com.Gachi_Gaja.server.dto.response.TotalRequirementResponseDTO;
import com.Gachi_Gaja.server.exception.AlreadyExistsException;
import com.Gachi_Gaja.server.repository.GroupRepository;
import com.Gachi_Gaja.server.repository.MemberRepository;
import com.Gachi_Gaja.server.repository.RequirementRepository;
import com.Gachi_Gaja.server.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RequirementService 단위 테스트")
class RequirementServiceTest {

    @Mock private RequirementRepository requirementRepository;
    @Mock private UserRepository userRepository;
    @Mock private GroupRepository groupRepository;
    @Mock private MemberRepository memberRepository;

    @InjectMocks
    private RequirementService requirementService;

    private UUID userId;
    private UUID groupId;
    private UUID requirementId;
    private User user;
    private Group group;
    private Member member;
    private Requirement requirement;
    private RequirementRequestDTO requestDTO;

    @BeforeEach
    void setUp() throws Exception {
        userId        = UUID.randomUUID();
        groupId       = UUID.randomUUID();
        requirementId = UUID.randomUUID();

        user = User.builder()
                .email("test@example.com")
                .password("encoded-password")
                .nickname("tester")
                .build();
        setPrivateField(user, "userId", userId);

        group = Group.builder()
                .title("test group").region("seoul").startingPoint("gangnam").endingPoint("hongdae")
                .transportation("subway").period("1 night 2 days").budget(100000)
                .startingDay(LocalDate.now().plusDays(10)).endingDay(LocalDate.now().plusDays(11))
                .requirementDeadline(LocalDate.now().plusDays(5)).voteDeadline(null).callCnt(3)
                .build();
        setPrivateField(group, "groupId", groupId);

        member = Member.builder().user(user).group(group).isLeader(false).build();

        requirement = Requirement.builder()
                .group(group).user(user).style("free").schedule("tight")
                .lodgingCriteria("budget").lodgingType("hotel").mealBudget("10k")
                .eatingHabit("any").distance("close").plusRequirement("none")
                .build();
        setPrivateField(requirement, "requirementId", requirementId);

        requestDTO = new RequirementRequestDTO(
                "free", "tight", "budget", "hotel",
                "10k", "any", "close", "none");
    }

    @Nested
    @DisplayName("generateRequirement")
    class GenerateRequirement {

        @Test
        @DisplayName("success: returns RequirementResponseDTO")
        void success() {
            given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(memberRepository.findByGroupAndUser(group, user)).willReturn(Optional.of(member));
            given(requirementRepository.existsByUserAndGroup(user, group)).willReturn(false);
            given(requirementRepository.save(any(Requirement.class))).willReturn(requirement);

            RequirementResponseDTO result = requirementService.generateRequirement(userId, groupId, requestDTO);

            assertThat(result).isNotNull();
            assertThat(result.style()).isEqualTo("free");
            assertThat(result.userId()).isEqualTo(userId);
            then(requirementRepository).should(times(1)).save(any(Requirement.class));
        }

        @Test
        @DisplayName("fail: AlreadyExistsException when requirement exists")
        void fail_alreadyExists() {
            given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(memberRepository.findByGroupAndUser(group, user)).willReturn(Optional.of(member));
            given(requirementRepository.existsByUserAndGroup(user, group)).willReturn(true);

            assertThatThrownBy(() -> requirementService.generateRequirement(userId, groupId, requestDTO))
                    .isInstanceOf(AlreadyExistsException.class);
            then(requirementRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("fail: EntityNotFoundException when group not found")
        void fail_groupNotFound() {
            given(groupRepository.findById(groupId)).willReturn(Optional.empty());
            assertThatThrownBy(() -> requirementService.generateRequirement(userId, groupId, requestDTO))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("fail: EntityNotFoundException when user not found")
        void fail_userNotFound() {
            given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
            given(userRepository.findById(userId)).willReturn(Optional.empty());
            assertThatThrownBy(() -> requirementService.generateRequirement(userId, groupId, requestDTO))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("fail: EntityNotFoundException when not a member")
        void fail_notMember() {
            given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(memberRepository.findByGroupAndUser(group, user)).willReturn(Optional.empty());
            assertThatThrownBy(() -> requirementService.generateRequirement(userId, groupId, requestDTO))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getRequirement")
    class GetRequirement {

        @Test
        @DisplayName("success: returns TotalRequirementResponseDTO with requirements")
        void success_withRequirement() {
            given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
            given(memberRepository.findAllByGroup(group)).willReturn(List.of(member));
            given(requirementRepository.findAllByGroup_GroupId(groupId)).willReturn(List.of(requirement));
            given(memberRepository.countByGroup(group)).willReturn(1L);

            TotalRequirementResponseDTO result = requirementService.getRequirement(groupId);

            assertThat(result).isNotNull();
            assertThat(result.totalMembers()).isEqualTo(1);
            assertThat(result.requirements()).hasSize(1);
            assertThat(result.requirements().get(0).requirementId()).isEqualTo(requirementId);
        }

        @Test
        @DisplayName("success: member without requirement has null requirementId")
        void success_memberWithoutRequirement() throws Exception {
            User user2 = User.builder().email("other@test.com").password("pw").nickname("other").build();
            setPrivateField(user2, "userId", UUID.randomUUID());
            Member member2 = Member.builder().user(user2).group(group).isLeader(false).build();

            given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
            given(memberRepository.findAllByGroup(group)).willReturn(List.of(member, member2));
            given(requirementRepository.findAllByGroup_GroupId(groupId)).willReturn(List.of(requirement));
            given(memberRepository.countByGroup(group)).willReturn(2L);

            TotalRequirementResponseDTO result = requirementService.getRequirement(groupId);

            assertThat(result.totalMembers()).isEqualTo(2);
            assertThat(result.requirements()).hasSize(2);
            long nullCount = result.requirements().stream()
                    .filter(r -> r.requirementId() == null).count();
            assertThat(nullCount).isEqualTo(1);
        }

        @Test
        @DisplayName("fail: EntityNotFoundException when group not found")
        void fail_groupNotFound() {
            given(groupRepository.findById(groupId)).willReturn(Optional.empty());
            assertThatThrownBy(() -> requirementService.getRequirement(groupId))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("putRequirement")
    class PutRequirement {

        @Test
        @DisplayName("success: updates requirement via dirty checking")
        void success() {
            given(requirementRepository.findById(requirementId)).willReturn(Optional.of(requirement));
            given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(memberRepository.findByGroupAndUser(group, user)).willReturn(Optional.of(member));

            RequirementRequestDTO updateDTO = new RequirementRequestDTO(
                    "healing", "relaxed", "premium", "resort", "50k", "vegan", "far", "ocean view");
            RequirementResponseDTO result =
                    requirementService.putRequirement(userId, groupId, requirementId, updateDTO);

            assertThat(result).isNotNull();
            then(requirementRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("fail: IllegalArgumentException when not owner")
        void fail_notOwner() throws Exception {
            UUID anotherUserId = UUID.randomUUID();
            User anotherUser = User.builder().email("another@test.com").password("pw").nickname("another").build();
            setPrivateField(anotherUser, "userId", anotherUserId);

            given(requirementRepository.findById(requirementId)).willReturn(Optional.of(requirement));
            given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
            given(userRepository.findById(anotherUserId)).willReturn(Optional.of(anotherUser));
            given(memberRepository.findByGroupAndUser(group, anotherUser)).willReturn(Optional.of(member));

            assertThatThrownBy(() ->
                    requirementService.putRequirement(anotherUserId, groupId, requirementId, requestDTO))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("fail: IllegalArgumentException when group mismatch")
        void fail_groupMismatch() throws Exception {
            UUID differentGroupId = UUID.randomUUID();
            Group differentGroup = Group.builder()
                    .title("other").region("busan").startingPoint("a").endingPoint("b")
                    .transportation("bus").period("2n3d").budget(200000)
                    .startingDay(LocalDate.now().plusDays(20)).endingDay(LocalDate.now().plusDays(22))
                    .requirementDeadline(LocalDate.now().plusDays(15)).voteDeadline(null).callCnt(3).build();
            setPrivateField(differentGroup, "groupId", differentGroupId);

            given(requirementRepository.findById(requirementId)).willReturn(Optional.of(requirement));
            given(groupRepository.findById(differentGroupId)).willReturn(Optional.of(differentGroup));
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(memberRepository.findByGroupAndUser(differentGroup, user)).willReturn(Optional.of(member));

            assertThatThrownBy(() ->
                    requirementService.putRequirement(userId, differentGroupId, requirementId, requestDTO))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("fail: EntityNotFoundException when requirement not found")
        void fail_requirementNotFound() {
            given(requirementRepository.findById(requirementId)).willReturn(Optional.empty());
            assertThatThrownBy(() ->
                    requirementService.putRequirement(userId, groupId, requirementId, requestDTO))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteRequirement")
    class DeleteRequirement {

        @Test
        @DisplayName("success: deleteById is called")
        void success() {
            given(requirementRepository.existsById(requirementId)).willReturn(true);
            given(groupRepository.existsById(groupId)).willReturn(true);
            given(requirementRepository.findById(requirementId)).willReturn(Optional.of(requirement));
            requirementService.deleteRequirement(groupId, requirementId);
            then(requirementRepository).should(times(1)).deleteById(requirementId);
        }

        @Test
        @DisplayName("fail: EntityNotFoundException when requirement not found")
        void fail_requirementNotFound() {
            given(requirementRepository.existsById(requirementId)).willReturn(false);
            assertThatThrownBy(() -> requirementService.deleteRequirement(groupId, requirementId))
                    .isInstanceOf(EntityNotFoundException.class);
            then(requirementRepository).should(never()).deleteById(any());
        }

        @Test
        @DisplayName("fail: IllegalArgumentException when group mismatch")
        void fail_groupMismatch() {
            UUID wrongGroupId = UUID.randomUUID();
            given(requirementRepository.existsById(requirementId)).willReturn(true);
            given(groupRepository.existsById(wrongGroupId)).willReturn(true);
            given(requirementRepository.findById(requirementId)).willReturn(Optional.of(requirement));
            assertThatThrownBy(() -> requirementService.deleteRequirement(wrongGroupId, requirementId))
                    .isInstanceOf(IllegalArgumentException.class);
            then(requirementRepository).should(never()).deleteById(any());
        }
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}

