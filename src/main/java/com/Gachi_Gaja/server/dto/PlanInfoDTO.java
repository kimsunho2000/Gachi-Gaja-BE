package com.Gachi_Gaja.server.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.Gachi_Gaja.server.domain.Plan;
import lombok.Builder;

@Builder
public record PlanInfoDTO(
        UUID planId,
        LocalDateTime startingTime,
        LocalDateTime endingTime,
        String location,
        String info,
        String transportation,
        int cost
) {

    public static PlanInfoDTO from(Plan plan) {
        return PlanInfoDTO.builder()
                .planId(plan.getPlanId())
                .startingTime(plan.getStartingTime())
                .endingTime(plan.getEndingTime())
                .location(plan.getLocation())
                .info(plan.getInfo())
                .transportation(plan.getTransportation())
                .cost(plan.getCost())
                .build();
    }

}
