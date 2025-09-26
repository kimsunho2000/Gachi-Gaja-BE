package com.Gachi_Gaja.server.dto.response;

public record GroupResponseDTO(
        String title,
        String region,
        String startingPlace,
        String endingPlace,
        String transportation,
        String period,
        int budget,
        String rDeadline,
        String pDeadline
) {}
