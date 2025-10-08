package com.Gachi_Gaja.server.dto;

import java.util.UUID;
import lombok.Builder;

@Builder
public record GroupInfoDTO(
        UUID groupId,
        String title,
        String region,
        String period,
        String role
) {}
