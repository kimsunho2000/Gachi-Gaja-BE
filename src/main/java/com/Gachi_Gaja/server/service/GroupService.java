package com.Gachi_Gaja.server.service;

import com.Gachi_Gaja.server.repository.CandidatePlanRepository;
import com.Gachi_Gaja.server.repository.GroupRepository;
import com.Gachi_Gaja.server.repository.MemberRepository;
import com.Gachi_Gaja.server.repository.PlanRepository;
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
    private final CandidatePlanRepository candidatePlanRepository;
    private final PlanRepository planRepository;
    private final InviteService inviteService;

    /** 1. 모임 생성 */
    @Transactional
    public UUID createGroup(GroupRequestDTO dto, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("해당 사용자가 존재하지 않습니다."));

        if (dto.getBudget() < 10000) {
            throw new IllegalArgumentException("예산은 10,000원 이상이어야 합니다.");
        }

        if (!dto.getStartingDay().isBefore(dto.getEndingDay())) {
            throw new IllegalArgumentException("시작일은 종료일보다 빨라야 합니다.");
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
                .startingDay(dto.getStartingDay())
                .endingDay(dto.getEndingDay())
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

        boolean hasCandidatePlan = !candidatePlanRepository.findAllByGroup_GroupId(groupId).isEmpty();
        boolean hasPlan = planRepository.existsByGroup_GroupId(groupId);

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
                .startingDay(group.getStartingDay())
                .endingDay(group.getEndingDay())
                .hasCandidatePlan(hasCandidatePlan)
                .hasPlan(hasPlan)
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

        if (!dto.getStartingDay().isBefore(dto.getEndingDay())) {
            throw new IllegalArgumentException("시작일은 종료일보다 빨라야 합니다.");
        }

        // 영속성 컨텍스트로 수정
        group.update(dto);
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

    /**
     * 7. 모임 멤버 추가 (POST /api/groups/{groupId}/members)
     *
     * 명세상 "현재 로그인된 사용자를 해당 그룹에 추가" 로, /api/invites/{groupId} 와 동작 주체가 같다.
     * 참가 규칙(마감일 · 중복 · 정원)은 InviteService 한 곳에만 두고 여기서는 위임만 한다.
     * 과거에는 이 메서드가 정원 체크도 락도 없이 직접 save 해서,
     * 이쪽으로 들어오면 정원 5명을 얼마든지 넘길 수 있었다.
     *
     * 중복 가입 응답은 명세를 따른다.
     * MemberAppendRequest 명세: "이미 추가된 User인 경우에는 따로 내부 처리를 하지 않고 마찬가지로 200 띄움"
     * 그래서 예외를 던지는 joinGroup 이 아니라 결과값을 돌려주는 join 을 호출한다.
     * (예외를 잡는 방식은 트랜잭션이 rollback-only 로 마킹돼 커밋 시 UnexpectedRollbackException 이 난다)
     */
    @Transactional
    public void addMemberToGroup(UUID groupId, UUID userId) {
        // 중복이면 ALREADY_MEMBER 가 돌아오고, 명세대로 아무 일도 하지 않은 채 200 이 나간다.
        inviteService.join(userId, groupId);
    }
}
