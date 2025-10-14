package com.Gachi_Gaja.server.dto.response;

import com.Gachi_Gaja.server.domain.CandidatePlan;
import com.Gachi_Gaja.server.domain.Group;
import jakarta.persistence.*;
import lombok.Builder;

import java.util.UUID;

@Builder
public record CandidatePlanResponseDTO (

    UUID candidatePlanId,
    UUID groupId,
    String planContent,
    int voteCount,
    boolean isVoted

) {

    public static CandidatePlanResponseDTO from(CandidatePlan candidatePlan) {
        return CandidatePlanResponseDTO.builder()
                .candidatePlanId(candidatePlan.getCandidatePlanId())
                .groupId(candidatePlan.getGroup().getGroupId())
                .planContent(candidatePlan.getPlanContent())
                .voteCount(candidatePlan.getVoteCount())
                .isVoted(candidatePlan.isVoted())
                .build();
    }

}
