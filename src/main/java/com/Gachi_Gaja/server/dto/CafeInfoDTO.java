package com.Gachi_Gaja.server.dto;

import java.util.UUID;

public record CafeInfoDTO(
        UUID cafeId,
        String name,
        String location,
        String info
) {}
