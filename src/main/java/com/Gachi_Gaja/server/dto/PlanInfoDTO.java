package com.Gachi_Gaja.server.dto;

import java.time.LocalDateTime;

public record PlanInfoDTO(
        String planId,
        LocalDateTime startingTime,
        LocalDateTime endingTime,
        String location,
        String info,
        String transportation,
        int cost
) {}
