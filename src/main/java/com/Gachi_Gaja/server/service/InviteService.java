package com.Gachi_Gaja.server.service;

import com.Gachi_Gaja.server.domain.Group;
import com.Gachi_Gaja.server.domain.Member;
import com.Gachi_Gaja.server.domain.User;
import com.Gachi_Gaja.server.exception.AlreadyExistsException;
import com.Gachi_Gaja.server.exception.DeadlinePassedException;
import com.Gachi_Gaja.server.exception.GroupFullException;
import com.Gachi_Gaja.server.exception.NotFoundException;
import com.Gachi_Gaja.server.repository.GroupRepository;
import com.Gachi_Gaja.server.repository.MemberRepository;
import com.Gachi_Gaja.server.repository.UserRepository;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InviteService {

    static final int MAX_GROUP_MEMBERS = 5;
    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    /** 참가 시도 결과. 중복 가입을 예외가 아니라 결과값으로 돌려주기 위한 타입. */
    public enum JoinResult { JOINED, ALREADY_MEMBER }

    /**
     * 그룹 참가 규칙의 단일 구현. 모든 참가 경로는 이 메서드를 통과해야 한다.
     *
     * 중복 가입을 예외로 던지지 않고 결과값으로 돌려주는 이유:
     * 엔드포인트마다 중복 시 응답이 다르기 때문이다.
     *   POST /api/invites/{groupId}        → 409 (예외)
     *   POST /api/groups/{groupId}/members → 200 (명세: "이미 추가된 User인 경우 ... 마찬가지로 200")
     * 여기서 예외를 던지면 @Transactional 프록시 경계를 넘는 순간 트랜잭션이
     * rollback-only 로 마킹돼, 호출측에서 잡아도 커밋 시 UnexpectedRollbackException 이 난다.
     * 그래서 "정상 흐름인 중복"은 예외가 아니라 값으로 표현한다.
     */
    @Transactional
    public JoinResult join(UUID userId, UUID groupId) {

        // groupId로 Group 엔티티 조회 (비관적 락 적용)
        //
        // 주의: 이 잠금 조회가 반드시 트랜잭션의 "첫 읽기"여야 한다.
        // MySQL InnoDB의 기본 격리 수준 REPEATABLE READ에서 일반 SELECT의 스냅샷(read view)은
        // 트랜잭션 내 첫 일반 읽기 시점에 고정된다. 앞에 일반 읽기가 오면, 락을 기다리는 동안
        // 다른 트랜잭션이 INSERT 후 커밋해도 아래 countByGroup이 그 값을 보지 못한다.
        // (락은 걸렸는데 세어 본 값이 낡아 정원이 초과되던 원인)
        // SELECT ... FOR UPDATE는 잠금 읽기라 read view를 만들지 않고 항상 최신 커밋본을 읽으므로,
        // 락을 먼저 잡으면 그 이후의 첫 일반 SELECT가 새 스냅샷을 뜬다. 순서를 바꾸지 말 것.
        Group group = groupRepository.findByIdWithPessimisticLock(groupId)
                .orElseThrow(() -> new NotFoundException("해당 그룹을 찾을 수 없습니다."));

        // 사용자 체크
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 사용자를 찾을 수 없습니다."));

        // 마감일 체크
        LocalDate deadLine = group.getRequirementDeadline();
        if (deadLine == null || deadLine.isBefore(LocalDate.now())) {
            throw new DeadlinePassedException("의견 입력 기간이 종료되어 가입할 수 없습니다.");
        }

        // 중복 가입 체크
        if (memberRepository.existsByGroup_GroupIdAndUser_UserId(groupId, userId)) {
            return JoinResult.ALREADY_MEMBER;
        }

        // 인원 수 체크
        if (memberRepository.countByGroup(group) >= MAX_GROUP_MEMBERS) {
            throw new GroupFullException("이미 만원인 그룹입니다.");
        }

        // Member에 포함
        Member member = Member.builder()
                .group(group)
                .user(user)
                .isLeader(false)
                .build();

        memberRepository.save(member);

        return JoinResult.JOINED;
    }

    /**
     * POST /api/invites/{groupId} (초대 수락)
     *
     * 이 경로는 기존대로 중복 가입을 409 로 알린다.
     */
    @Transactional
    public ResponseEntity<Void> joinGroup(UUID userId, UUID groupId) {
        if (join(userId, groupId) == JoinResult.ALREADY_MEMBER) {
            throw new AlreadyExistsException("이미 해당 그룹에 참가 중입니다.");
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
