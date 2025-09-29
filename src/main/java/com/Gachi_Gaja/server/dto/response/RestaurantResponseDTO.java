package com.Gachi_Gaja.server.dto.response;

import com.Gachi_Gaja.server.dto.RestaurantInfoDTO;

import java.util.List;

/*
    식당 정보시 조회 DTO
 */
public record RestaurantResponseDTO(
        List<RestaurantInfoDTO> restaurantInfoList
) {}
