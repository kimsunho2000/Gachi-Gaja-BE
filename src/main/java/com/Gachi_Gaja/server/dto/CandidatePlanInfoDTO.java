package com.Gachi_Gaja.server.dto;

import java.util.UUID;

import com.Gachi_Gaja.server.domain.CandidatePlan;
import com.Gachi_Gaja.server.dto.response.CandidatePlanResponseDTO;
import lombok.Builder;

@Builder
public record CandidatePlanInfoDTO(
   UUID candidatePlanId,
   String content,
   int votes,
   boolean voted
) {
    public static CandidatePlanInfoDTO from(CandidatePlan candidatePlan, boolean voted) {
        return CandidatePlanInfoDTO.builder()
                .candidatePlanId(candidatePlan.getCandidatePlanId())
                .content(candidatePlan.getPlanContent())
                .votes(candidatePlan.getVoteCount())
                .voted(voted)
                .build();
    }
}
