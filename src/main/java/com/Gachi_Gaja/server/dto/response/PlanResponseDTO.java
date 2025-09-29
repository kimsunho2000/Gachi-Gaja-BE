package com.Gachi_Gaja.server.dto.response;

import com.Gachi_Gaja.server.dto.PlanInfoDTO;
import java.util.List;

public record PlanResponseDTO(
        boolean isLeader, List<PlanInfoDTO> planList
) {}
