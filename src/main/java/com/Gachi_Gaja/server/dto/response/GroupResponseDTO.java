package com.Gachi_Gaja.server.dto.response;

import java.time.LocalDate;
import lombok.Builder;

@Builder
public record GroupResponseDTO(
        String title,
        String region,
        String startingPlace,
        String endingPlace,
        String transportation,
        String period,
        int budget,
        String rDeadline,
        String pDeadline,
        LocalDate startingDay,
        LocalDate endingDay,
        boolean hasCandidatePlan,
        boolean hasPlan
) {}
