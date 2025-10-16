package com.Gachi_Gaja.server.service;

import com.Gachi_Gaja.server.repository.UserRepository;
import com.Gachi_Gaja.server.domain.Group;
import com.Gachi_Gaja.server.domain.Member;
import com.Gachi_Gaja.server.domain.Requirement;
import com.Gachi_Gaja.server.domain.User;
import com.Gachi_Gaja.server.repository.GroupRepository;
import com.Gachi_Gaja.server.repository.MemberRepository;
import com.Gachi_Gaja.server.repository.RequirementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MockService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final RequirementRepository requirementRepository;
    private final MemberRepository memberRepository;

    // 테스트용 Group mock 생성 메서드 추후 삭제 예정
    @Transactional
    public Group createMockGroup() {
        Group mockGroup = Group.builder()
                .title("제주도 여행")
                .region("제주도")
                .startingPoint("제주공항")
                .endingPoint("제주공항")
                .transportation("렌트카")
                .period("2박 3일")
                .budget(500000)
                .requirementDeadline(LocalDate.now().plusDays(3))
                .voteDeadline(LocalDate.now().plusDays(7))
                .build();
        return groupRepository.save(mockGroup);
    }

    // 테스트용 User mock 생성 메서드 추후 삭제 예정
    @Transactional
    public User createMockUser(int num) {
        String testEmail = "test@example.com" + num;

        return userRepository.findByEmail(testEmail)
                .orElseGet(() -> {
                    User mockUser = User.builder()
                            .email(testEmail)
                            .password("password123")
                            .nickname("testuser" + num)
                            .build();
                    return userRepository.save(mockUser);
                });
    }

    // 테스트용 Member mock 생성(있으면 재사용)
    @Transactional
    public Member createMockMember(User user, Group group) {
        return memberRepository.findByGroupAndUser(group, user)
                .orElseGet(() -> {
                    Member mockMember = Member.builder()
                            .group(group)
                            .user(user)
                            .isLeader(false)
                            .build();
                    return memberRepository.save(mockMember);
                });
    }

    /* 테스트용 Requirement mock 생성 및 ID 반환 메서드
       리스트 반환값은 userId, groupId, requirementId
     */
    @Transactional
    public List<UUID> generateMockIds() {
        User mockUser1 = createMockUser(1);
        User mockUser2 = createMockUser(2);
        User mockUser3 = createMockUser(3);
        Group mockGroup = createMockGroup();
        createMockMember(mockUser1, mockGroup);
        createMockMember(mockUser2, mockGroup);
        createMockMember(mockUser3, mockGroup);

        Requirement mockRequirement1 = Requirement.builder()
                .user(mockUser1)
                .group(mockGroup)
                .style("모던")
                .restaurants(Arrays.asList(UUID.randomUUID(), UUID.randomUUID()))
                .attractions(List.of(UUID.randomUUID()))
                .cafes(List.of(UUID.randomUUID()))
                .build();

        Requirement mockRequirement2 = Requirement.builder()
                .user(mockUser2)
                .group(mockGroup)
                .style("레트로")
                .restaurants(Arrays.asList(UUID.randomUUID(), UUID.randomUUID()))
                .attractions(List.of(UUID.randomUUID()))
                .cafes(List.of(UUID.randomUUID()))
                .build();

        Requirement savedRequirement1 = requirementRepository.save(mockRequirement1);
        requirementRepository.save(mockRequirement2);
        return Arrays.asList(mockUser1.getUserId(), mockGroup.getGroupId(), savedRequirement1.getRequirementId());
    }

    @Transactional
    public List<UUID> generateMockId() {
        User mockUser1 = createMockUser(1);
        Group mockGroup = createMockGroup();
        createMockMember(mockUser1, mockGroup);

        Requirement mockRequirement1 = Requirement.builder()
                .user(mockUser1)
                .group(mockGroup)
                .style("모던")
                .restaurants(Arrays.asList(UUID.randomUUID(), UUID.randomUUID()))
                .attractions(List.of(UUID.randomUUID()))
                .cafes(List.of(UUID.randomUUID()))
                .build();

        Requirement savedRequirement1 = requirementRepository.save(mockRequirement1);
        return Arrays.asList(mockUser1.getUserId(), mockGroup.getGroupId(), savedRequirement1.getRequirementId());
    }
}
