package com.Gachi_Gaja.server.dto.response;

import java.util.UUID;

public record UserResponseDTO(
        UUID userId,
        String nickName,
        String email
) {}
