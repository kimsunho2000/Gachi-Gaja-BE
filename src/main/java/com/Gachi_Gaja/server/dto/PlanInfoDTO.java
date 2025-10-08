package com.Gachi_Gaja.server.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record PlanInfoDTO(
        UUID planId,
        LocalDateTime startingTime,
        LocalDateTime endingTime,
        String location,
        String info,
        String transportation,
        int cost
) {}
