package com.Gachi_Gaja.server.service;

import com.Gachi_Gaja.server.domain.Group;
import com.Gachi_Gaja.server.domain.Member;
import com.Gachi_Gaja.server.domain.User;
import com.Gachi_Gaja.server.dto.request.GroupRequestDTO;
import com.Gachi_Gaja.server.dto.response.GroupResponseDTO;
import com.Gachi_Gaja.server.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupService unit test")
class GroupServiceTest {

    @Mock private GroupRepository groupRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private UserRepository userRepository;
    @Mock private CandidatePlanRepository candidatePlanRepository;
    @Mock private PlanRepository planRepository;

    @InjectMocks
    private GroupService groupService;

    private UUID userId;
    private UUID groupId;
    private User user;
    private Group group;
    private Member leaderMember;
    private Member regularMember;
    private GroupRequestDTO validDTO;

    @BeforeEach
    void setUp() throws Exception {
        userId  = UUID.randomUUID();
        groupId = UUID.randomUUID();

        user = User.builder().email("leader@example.com").password("pw").nickname("leader").build();
        setPrivateField(user, "userId", userId);

        group = Group.builder()
                .title("test trip").region("jeju").startingPoint("airport").endingPoint("airport")
                .transportation("car").period("2n3d").budget(300000)
                .startingDay(LocalDate.now().plusDays(30)).endingDay(LocalDate.now().plusDays(32))
                .requirementDeadline(LocalDate.now().plusDays(20)).voteDeadline(null).callCnt(3)
                .build();
        setPrivateField(group, "groupId", groupId);

        leaderMember = Member.builder().user(user).group(group).isLeader(true).build();
        regularMember = Member.builder().user(user).group(group).isLeader(false).build();

        validDTO = new GroupRequestDTO(
                "test trip", "jeju", "airport", "airport",
                LocalDate.now().plusDays(30), LocalDate.now().plusDays(32),
                "car", "2박 3일", 300000, LocalDate.now().plusDays(20));
    }

    @Nested
    @DisplayName("createGroup")
    class CreateGroup {

        @Test
        @DisplayName("success: returns groupId")
        void success() {
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(groupRepository.save(any(Group.class))).willAnswer(invocation -> {
                Group savedGroup = invocation.getArgument(0);
                setPrivateField(savedGroup, "groupId", groupId);
                return savedGroup;
            });
            UUID result = groupService.createGroup(validDTO, userId);
            assertThat(result).isEqualTo(groupId);
            then(memberRepository).should(times(1)).save(any(Member.class));
        }

        @Test
        @DisplayName("fail: budget too low")
        void fail_budgetTooLow() {
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            GroupRequestDTO dto = new GroupRequestDTO(
                    "t", "s", "a", "b",
                    LocalDate.now().plusDays(10), LocalDate.now().plusDays(11),
                    "sub", "1박 2일", 5000, LocalDate.now().plusDays(5));
            assertThatThrownBy(() -> groupService.createGroup(dto, userId))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("fail: invalid date range")
        void fail_invalidDateRange() {
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            GroupRequestDTO dto = new GroupRequestDTO(
                    "t", "s", "a", "b",
                    LocalDate.now().plusDays(11), LocalDate.now().plusDays(10),
                    "sub", "1박 2일", 100000, LocalDate.now().plusDays(5));
            assertThatThrownBy(() -> groupService.createGroup(dto, userId))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("fail: user not found")
        void fail_userNotFound() {
            given(userRepository.findById(userId)).willReturn(Optional.empty());
            assertThatThrownBy(() -> groupService.createGroup(validDTO, userId))
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("getGroupById")
    class GetGroupById {

        @Test
        @DisplayName("success: returns GroupResponseDTO")
        void success() {
            given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
            given(candidatePlanRepository.findAllByGroup_GroupId(groupId)).willReturn(List.of());
            given(planRepository.existsByGroup_GroupId(groupId)).willReturn(false);

            GroupResponseDTO result = groupService.getGroupById(groupId);

            assertThat(result).isNotNull();
            assertThat(result.title()).isEqualTo("test trip");
            assertThat(result.region()).isEqualTo("jeju");
        }

        @Test
        @DisplayName("fail: group not found")
        void fail_groupNotFound() {
            given(groupRepository.findById(groupId)).willReturn(Optional.empty());
            assertThatThrownBy(() -> groupService.getGroupById(groupId))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("updateGroup")
    class UpdateGroup {

        @Test
        @DisplayName("success: leader can update")
        void success_leader() {
            given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(memberRepository.findByGroupAndUser(group, user)).willReturn(Optional.of(leaderMember));
            assertThatNoException().isThrownBy(() -> groupService.updateGroup(groupId, userId, validDTO));
        }

        @Test
        @DisplayName("fail: non-leader gets AccessDeniedException")
        void fail_notLeader() {
            given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(memberRepository.findByGroupAndUser(group, user)).willReturn(Optional.of(regularMember));
            assertThatThrownBy(() -> groupService.updateGroup(groupId, userId, validDTO))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("deleteGroup")
    class DeleteGroup {

        @Test
        @DisplayName("success: leader can delete")
        void success_leader() {
            given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(memberRepository.findByGroupAndUser(group, user)).willReturn(Optional.of(leaderMember));
            groupService.deleteGroup(groupId, userId);
            then(groupRepository).should(times(1)).delete(group);
        }

        @Test
        @DisplayName("fail: non-leader gets AccessDeniedException")
        void fail_notLeader() {
            given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(memberRepository.findByGroupAndUser(group, user)).willReturn(Optional.of(regularMember));
            assertThatThrownBy(() -> groupService.deleteGroup(groupId, userId))
                    .isInstanceOf(AccessDeniedException.class);
            then(groupRepository).should(never()).delete(any());
        }
    }

    @Nested
    @DisplayName("pessimistic lock metadata")
    class PessimisticLockPath {

        @Test
        @DisplayName("findByIdWithPessimisticLock exists with correct signature")
        void pessimisticLock_methodExists() throws NoSuchMethodException {
            var method = GroupRepository.class.getMethod("findByIdWithPessimisticLock", UUID.class);
            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(Optional.class);
        }

        @Test
        @DisplayName("LOCK_TIMEOUT is 3000ms")
        void pessimisticLock_timeoutIs3000ms() throws Exception {
            var field = GroupRepository.class.getField("LOCK_TIMEOUT");
            field.setAccessible(true);
            String timeout = (String) field.get(null);
            assertThat(timeout).isEqualTo("3000");
        }
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}

