package com.Gachi_Gaja.server.service;

import com.Gachi_Gaja.server.repository.GroupRepository;
import com.Gachi_Gaja.server.repository.MemberRepository;
import com.Gachi_Gaja.server.repository.UserRepository;
import com.Gachi_Gaja.server.domain.Group;
import com.Gachi_Gaja.server.domain.Member;
import com.Gachi_Gaja.server.domain.User;
import com.Gachi_Gaja.server.dto.GroupInfoDTO;
import com.Gachi_Gaja.server.dto.MemberInfoDTO;
import com.Gachi_Gaja.server.dto.request.GroupRequestDTO;
import com.Gachi_Gaja.server.dto.response.GroupListResponseDTO;
import com.Gachi_Gaja.server.dto.response.GroupResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import java.util.NoSuchElementException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupService {

    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;

    /** 1. 모임 생성 */
    @Transactional
    public UUID createGroup(GroupRequestDTO dto, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("해당 사용자가 존재하지 않습니다."));

        if (dto.getBudget() < 10000) {
            throw new IllegalArgumentException("예산은 10,000원 이상이어야 합니다.");
        }

        // 그룹 생성
        Group group = Group.builder()
                .title(dto.getTitle())
                .region(dto.getRegion())
                .startingPoint(dto.getStartingPlace())
                .endingPoint(dto.getEndingPlace())
                .transportation(dto.getTransportation())
                .period(dto.getPeriod())
                .budget(dto.getBudget())
                .requirementDeadline(dto.getRDeadline())
                .voteDeadline(null)
                .callCnt(3)
                .build();

        groupRepository.save(group);

        // 리더(Member) 등록
        Member leader = Member.builder()
                .user(user)
                .group(group)
                .isLeader(true)
                .build();

        memberRepository.save(leader);

        return group.getGroupId();
    }

    /** 2. 가입한 모임 전체 조회 (userId 기반) */
    @Transactional(readOnly = true)
    public GroupListResponseDTO getGroupsByUser(UUID userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("해당 사용자가 존재하지 않습니다."));

        List<Member> members = memberRepository.findByUser_UserId(userId);
        List<GroupInfoDTO> groupList = members.stream()
                .map(m -> {
                    Group g = m.getGroup();
                    return GroupInfoDTO.builder()
                            .groupId(g.getGroupId())
                            .title(g.getTitle())
                            .region(g.getRegion())
                            .period(g.getPeriod())
                            .role(m.isLeader() ? "LEADER" : "MEMBER")
                            .build();
                })
                .collect(Collectors.toList());

        return GroupListResponseDTO.from(groupList);
    }

    /** 3. 모임 상세 조회 */
    @Transactional(readOnly = true)
    public GroupResponseDTO getGroupById(UUID groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("해당 그룹이 존재하지 않습니다."));
        return GroupResponseDTO.builder()
                .title(group.getTitle())
                .region(group.getRegion())
                .startingPlace(group.getStartingPoint())
                .endingPlace(group.getEndingPoint())
                .transportation(group.getTransportation())
                .period(group.getPeriod())
                .budget(group.getBudget())
                .rDeadline(String.valueOf(group.getRequirementDeadline()))
                .pDeadline(String.valueOf(group.getVoteDeadline()))
                .build();
    }

    /** 4. 모임 삭제 */
    @Transactional
    public void deleteGroup(UUID groupId, UUID userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("해당 그룹이 존재하지 않습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("해당 사용자가 존재하지 않습니다."));

        Member member = memberRepository.findByGroupAndUser(group, user)
                .orElseThrow(() -> new NoSuchElementException("해당 그룹에 속하지 않은 사용자입니다."));

        //리더 검증
        if (!member.isLeader()) {
            throw new  AccessDeniedException("리더만 모임을 삭제할 수 있습니다.");
        }

        groupRepository.delete(group);
    }

    /** 5. 모임 정보 수정 */
    @Transactional
    public void updateGroup(UUID groupId, UUID userId, GroupRequestDTO dto) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("해당 그룹이 존재하지 않습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("해당 사용자가 존재하지 않습니다."));

        Member member = memberRepository.findByGroupAndUser(group, user)
                .orElseThrow(() -> new NoSuchElementException("해당 그룹에 속하지 않은 사용자입니다."));

        //리더 권한 확인
        if (!member.isLeader()) {
            throw new AccessDeniedException("리더만 그룹 정보를 수정할 수 있습니다.");
        }

        if (dto.getBudget() < 10000) {
            throw new IllegalArgumentException("예산은 10,000원 이상이어야 합니다.");
        }

        // 필드 수정
        group = Group.builder()
                .title(dto.getTitle())
                .region(dto.getRegion())
                .startingPoint(dto.getStartingPlace())
                .endingPoint(dto.getEndingPlace())
                .transportation(dto.getTransportation())
                .period(dto.getPeriod())
                .budget(dto.getBudget())
                .requirementDeadline(dto.getRDeadline())
                .voteDeadline(null)
                .build();

        groupRepository.save(group);
    }

    /** 6. 모임 멤버 조회 */
    @Transactional(readOnly = true)
    public List<MemberInfoDTO> getGroupMembers(UUID groupId) {

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("해당 그룹이 존재하지 않습니다."));

        List<Member> members = memberRepository.findByGroup_GroupId(groupId);

        return members.stream()
                .map(m -> MemberInfoDTO.builder()
                        .memberId(m.getMemberId())
                        .userId(m.getUser().getUserId())
                        .nickname(m.getUser().getNickname())
                        .role(m.isLeader() ? "LEADER" : "MEMBER")
                        .build())
                .collect(Collectors.toList());
    }

    /** 7. 모임 멤버 추가 */
    @Transactional
    public void addMemberToGroup(UUID groupId, UUID userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("해당 그룹이 존재하지 않습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("해당 사용자가 존재하지 않습니다."));

        boolean exists = memberRepository.findByGroupAndUser(group, user).isPresent();
        if (exists) return;

        Member member = Member.builder()
                .user(user)
                .group(group)
                .isLeader(false)
                .build();

        memberRepository.save(member);
    }
}