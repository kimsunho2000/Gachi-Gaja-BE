package com.Gachi_Gaja.server.dto;

public record RestaurantInfoDTO(
        String restaurantId,
        String name,
        String region,
        String location,
        String info
){}
