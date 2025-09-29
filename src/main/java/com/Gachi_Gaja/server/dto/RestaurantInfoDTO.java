package com.Gachi_Gaja.server.dto;

import java.util.UUID;

public record RestaurantInfoDTO(
        UUID restaurantId,
        String name,
        String region,
        String location,
        String info
){}
