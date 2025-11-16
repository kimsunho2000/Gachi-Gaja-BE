package com.Gachi_Gaja.server.dto.request;

import com.Gachi_Gaja.server.domain.Group;
import com.Gachi_Gaja.server.domain.Plan;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor

/*
여행 계획 수정을 위한 DTO
 */
public class PlanRequestDTO {

    private LocalDateTime startingTime;
    private LocalDateTime endingTime;
    private String location;
    private String info;
    private String transportation;
    private int cost;

    public Plan toEntity(Group group) {
        return Plan.builder()
                .group(group)
                .startingTime(this.startingTime)
                .endingTime(this.endingTime)
                .location(this.location)
                .info(this.info)
                .transportation(this.transportation)
                .cost(this.cost)
                .build();
    }

}
