package com.Gachi_Gaja.server.service;

import com.Gachi_Gaja.server.repository.UserRepository;
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
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequirementService {

    private final RequirementRepository requirementRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;

    private Group getGroupById(UUID groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("그룹을 찾을 수 없습니다."));
    }

    private User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
    }

    private Requirement getRequirementById(UUID requirementId) {
        return requirementRepository.findById(requirementId)
                .orElseThrow(() -> new EntityNotFoundException("요구 사항을 찾을 수 없습니다."));
    }

    private Member getMemberByGroupAndUser(Group group, User user) {
        return memberRepository.findByGroupAndUser(group, user)
                .orElseThrow(() -> new EntityNotFoundException("여행모임원에서 찾을 수 없습니다."));
    }

    private void validateGroupExists(UUID groupId) {
        if (!groupRepository.existsById(groupId)) {
            throw new EntityNotFoundException("그룹을 찾을 수 없습니다.");
        }
    }

    private void validateRequirementExists(UUID requirementId) {
        if (!requirementRepository.existsById(requirementId)) {
            throw new EntityNotFoundException("요구 사항을 찾을 수 없습니다.");
        }
    }

    private void validateRequirementNotExists(User user, Group group) {
        if (requirementRepository.existsByUserAndGroup(user, group)) {
            throw new AlreadyExistsException("이미 요구사항이 존재합니다.");
        }
    }

    private List<UUID> convertToUuidList(List<String> stringUuids) {
        if (stringUuids == null) {
            return List.of();
        }
        return stringUuids.stream()
                .map(UUID::fromString)
                .collect(Collectors.toList());
    }

    @Transactional
    public RequirementResponseDTO generateRequirement(UUID userId, UUID groupId, RequirementRequestDTO dto) {
        // 엔티티 조회
        Group group = getGroupById(groupId);
        User user = getUserById(userId);
        Member member = getMemberByGroupAndUser(group, user);

        // 중복 검증
        validateRequirementNotExists(user, group);

        // 요구사항 생성 및 저장
        Requirement requirement = dto.toEntity(group, user);
        Requirement savedRequirement = requirementRepository.save(requirement);

        return RequirementResponseDTO.from(savedRequirement, user, member);
    }

    @Transactional(readOnly = true)
    public TotalRequirementResponseDTO getRequirement(UUID groupId) {
        // 그룹 조회
        Group group = getGroupById(groupId);

        // 그룹의 모든 멤버 조회
        List<Member> allMembers = memberRepository.findAllByGroup(group);

        // 그룹의 모든 요구사항을 User 기준 Map으로 변환
        Map<User, Requirement> requirementMap = requirementRepository.findAllByGroup_GroupId(groupId).stream()
                .collect(Collectors.toMap(Requirement::getUser, Function.identity()));

        // 멤버별 요구사항 DTO 생성 (요구사항 없으면 기본 정보만)
        List<RequirementResponseDTO> items = allMembers.stream()
                .map(member -> {
                    User user = member.getUser();
                    Requirement requirement = requirementMap.get(user);
                    return (requirement != null)
                            ? RequirementResponseDTO.from(requirement, user, member)
                            : RequirementResponseDTO.from(user, member);
                })
                .toList();

        int totalMembers = (int) memberRepository.countByGroup(group);

        return TotalRequirementResponseDTO.from(totalMembers, items);
    }

    @Transactional
    public RequirementResponseDTO putRequirement(UUID userId, UUID groupId, UUID requirementId, RequirementRequestDTO dto) {
        // 엔티티 조회
        Requirement requirement = getRequirementById(requirementId);
        Group group = getGroupById(groupId);
        User user = getUserById(userId);
        Member member = getMemberByGroupAndUser(group, user);

        // 요구사항 작성자와 요청 사용자가 동일한지 확인
        if (!requirement.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 요구사항만 수정할 수 있습니다.");
        }

        // 요구사항이 해당 그룹에 속하는지 확인
        if (!requirement.getGroup().getGroupId().equals(groupId)) {
            throw new IllegalArgumentException("해당 그룹의 요구사항이 아닙니다.");
        }

        // 영속성 컨텍스트로 업데이트
        dto.update(requirement);

        return RequirementResponseDTO.from(requirement, user, member);
    }

    @Transactional
    public void deleteRequirement(UUID groupId, UUID requirementId) {
        // 존재 여부 확인
        validateRequirementExists(requirementId);
        validateGroupExists(groupId);

        // 요구사항 조회하여 그룹 일치 여부 확인
        Requirement requirement = getRequirementById(requirementId);
        if (!requirement.getGroup().getGroupId().equals(groupId)) {
            throw new IllegalArgumentException("해당 그룹의 요구사항이 아닙니다.");
        }

        // 삭제
        requirementRepository.deleteById(requirementId);

    }
}
