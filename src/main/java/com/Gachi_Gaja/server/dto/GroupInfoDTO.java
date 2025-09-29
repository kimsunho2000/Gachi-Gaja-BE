package com.Gachi_Gaja.server.dto;

import java.util.UUID;

public record GroupInfoDTO(
        UUID groupId,
        String title,
        String region,
        String period,
        String role
) {}
