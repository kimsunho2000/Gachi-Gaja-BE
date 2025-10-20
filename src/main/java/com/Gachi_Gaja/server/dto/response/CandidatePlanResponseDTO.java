package com.Gachi_Gaja.server.dto.response;

import com.Gachi_Gaja.server.domain.CandidatePlan;
import com.Gachi_Gaja.server.domain.Group;
import com.Gachi_Gaja.server.dto.CandidatePlanInfoDTO;
import jakarta.persistence.*;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record CandidatePlanResponseDTO (

        UUID groupId,
        boolean isLeader,
        int callCnt,    // AI 호출 횟수
        List<CandidatePlanInfoDTO> candidatePlans

) {

    public static CandidatePlanResponseDTO from(UUID groupId, boolean isLeader, int callCnt, List<CandidatePlanInfoDTO> candidatePlans) {
        return CandidatePlanResponseDTO.builder()
                .groupId(groupId)
                .isLeader(isLeader)
                .callCnt(callCnt)
                .candidatePlans(candidatePlans)
                .build();
    }

}
