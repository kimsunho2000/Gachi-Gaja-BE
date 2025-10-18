package com.Gachi_Gaja.server.dto.response;

import com.Gachi_Gaja.server.dto.PlanInfoDTO;
import lombok.Builder;

import java.util.List;

@Builder
public record PlanResponseDTO(
        boolean isLeader, List<PlanInfoDTO> planList
) {}
