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

    @Transactional
    public ResponseEntity<Void> joinGroup(UUID userId, UUID groupId) {

        // 사용자 체크
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 사용자를 찾을 수 없습니다."));

        // groupId로 Group 엔티티 조회 (비관적 락 적용)
        Group group = groupRepository.findByIdWithPessimisticLock(groupId)
                .orElseThrow(() -> new NotFoundException("해당 그룹을 찾을 수 없습니다."));

        // 마감일 체크
        LocalDate deadLine = group.getRequirementDeadline();
        if (deadLine == null || deadLine.isBefore(LocalDate.now())) {
            throw new DeadlinePassedException("의견 입력 기간이 종료되어 가입할 수 없습니다.");
        }

        // 중복 가입 체크
        if (memberRepository.existsByGroup_GroupIdAndUser_UserId(groupId, userId)) {
            throw new AlreadyExistsException("이미 해당 그룹에 참가 중입니다.");
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

        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
