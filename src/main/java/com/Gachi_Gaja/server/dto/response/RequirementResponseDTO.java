package com.Gachi_Gaja.server.dto.response;

import com.Gachi_Gaja.server.dto.AttractionInfoDTO;
import com.Gachi_Gaja.server.dto.CafeInfoDTO;
import com.Gachi_Gaja.server.dto.RestaurantInfoDTO;
import lombok.Builder;

import java.util.List;

@Builder
public record RequirementResponseDTO(
        String style,
        String Schedule,
        String lodgingCriteria,
        String lodgingType,
        String mealBudget,
        String eatingHabit,
        String distance,
        String plusRequirement,
        List<RestaurantInfoDTO> restaurantInfoList,
        List<AttractionInfoDTO> attractionInfoList,
        List<CafeInfoDTO> cafeInfoList
) {}
