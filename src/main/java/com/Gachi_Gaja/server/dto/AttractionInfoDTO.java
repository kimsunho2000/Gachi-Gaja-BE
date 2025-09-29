package com.Gachi_Gaja.server.dto;

import java.util.UUID;

public record AttractionInfoDTO(
        UUID attractionId,
        String name,
        String region,
        String location,
        String info
) {}
