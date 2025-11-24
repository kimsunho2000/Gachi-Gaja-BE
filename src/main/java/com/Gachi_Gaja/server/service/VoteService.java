package com.Gachi_Gaja.server.service;

import com.Gachi_Gaja.server.domain.*;
import com.Gachi_Gaja.server.domain.CandidatePlan;
import com.Gachi_Gaja.server.domain.Member;
import com.Gachi_Gaja.server.domain.MemberVote;
import com.Gachi_Gaja.server.dto.CandidatePlanInfoDTO;
import com.Gachi_Gaja.server.dto.request.VoteRequestDTO;
import com.Gachi_Gaja.server.dto.response.VoteResponseDTO;
import com.Gachi_Gaja.server.exception.NotFoundException;
import com.Gachi_Gaja.server.repository.*;

import com.Gachi_Gaja.server.repository.CandidatePlanRepository;
import com.Gachi_Gaja.server.repository.GroupRepository;
import com.Gachi_Gaja.server.repository.MemberRepository;
import com.Gachi_Gaja.server.repository.MemberVoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final CandidatePlanRepository candidatePlanRepository;
    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final MemberVoteRepository memberVoteRepository;

    // 📌 (1) 투표 조회
    public VoteResponseDTO getVote(UUID groupId, UUID userId) {

        // 후보 목록 조회
        List<CandidatePlan> plans =
                candidatePlanRepository.findAllByGroup_GroupId(groupId);

        // userId + groupId → Member 조회
        Member member = memberRepository
                .findByUser_UserIdAndGroup_GroupId(userId, groupId)
                .orElseThrow(() -> new NotFoundException("해당 그룹의 멤버가 아닙니다."));

        // 기존 투표 조회
        UUID votedPlanId = memberVoteRepository
                .findByUser_UserIdAndGroup_GroupId(userId, groupId)
                .map(v -> v.getCandidatePlan().getCandidatePlanId())
                .orElse(null);

        // 후보 + 투표 여부 표시
        List<CandidatePlanInfoDTO> planList =
                plans.stream()
                        .map(plan -> CandidatePlanInfoDTO.from(
                                plan,
                                plan.getCandidatePlanId().equals(votedPlanId)
                        ))
                        .toList();

        return new VoteResponseDTO(
                false,
                false,
                LocalDate.now(),
                planList
        );
    }


    // 📌 (2) 투표 생성/수정
    @Transactional
    public void createVote(UUID groupId, UUID userId, VoteRequestDTO request) {

        UUID planId = UUID.fromString(request.candidatePlanId());

        groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group 없음"));

        Member member = memberRepository
                .findByUser_UserIdAndGroup_GroupId(userId, groupId)
                .orElseThrow(() -> new NotFoundException("Member 없음"));

        CandidatePlan newPlan = candidatePlanRepository.findById(planId)
                .orElseThrow(() -> new NotFoundException("후보 없음"));


        // ⭐ 기존 투표 조회
        MemberVote vote = memberVoteRepository
                .findByUser_UserIdAndGroup_GroupId(userId, groupId)
                .orElse(null);

        if (vote != null) {
            // 기존 후보 -1
            CandidatePlan oldPlan = vote.getCandidatePlan();
            oldPlan.updateVoteCount(oldPlan.getVoteCount() - 1);
            candidatePlanRepository.save(oldPlan);

            // 기존 vote 엔티티에 새 후보로 교체
            vote.setCandidatePlan(newPlan);

        } else {
            // 첫 투표: 새 vote 객체 생성
            vote = MemberVote.builder()
                    .user(member.getUser())
                    .group(member.getGroup())
                    .candidatePlan(newPlan)
                    .build();
        }

        // 신규 후보 +1
        newPlan.updateVoteCount(newPlan.getVoteCount() + 1);
        candidatePlanRepository.save(newPlan);

        // 변경된 vote 저장
        memberVoteRepository.save(vote);
    }

    // 📌 (3) 투표 수정 = 생성과 동일
    @Transactional
    public void updateVote(UUID groupId, UUID userId, VoteRequestDTO request) {
        createVote(groupId, userId, request);
    }
}