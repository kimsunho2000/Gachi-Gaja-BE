package com.Gachi_Gaja.server.dto.request;

import com.Gachi_Gaja.server.domain.Group;
import com.Gachi_Gaja.server.domain.Requirement;
import com.Gachi_Gaja.server.domain.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
/*
    요구사항 생성 및 수정시의 DTO
 */
public class RequirementRequestDTO {

    String style;
    String schedule;
    String lodgingCriteria;
    String lodgingType;
    String mealBudget;
    String eatingHabit;
    String distance;
    String plusRequirement;
    List<String> restaurants;
    List<String> attractions;
    List<String> cafes;

    /*
      각 객체별 UUID 리스트
      식당, 관광지, 카페를 nullable = true로 변경하면서 수정
     */
    /*
    @NotEmpty
    List<@Pattern(
            regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
            message = "올바른 UUID 형식이 아닙니다."
    )String> restaurants;

    @NotEmpty
    List<@Pattern(
            regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
            message = "올바른 UUID 형식이 아닙니다."
    )String> attractions;

    @NotEmpty
    List<@Pattern(
            regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
            message = "올바른 UUID 형식이 아닙니다."
    )String> cafes;
     */

    public Requirement toEntity(Group group, User user, List<UUID> restaurants, List<UUID> attractions, List<UUID> cafes) {
        return Requirement.builder()
                .group(group)
                .user(user)
                .style(this.style)
                .schedule(this.schedule)
                .lodgingCriteria(this.lodgingCriteria)
                .lodgingType(this.lodgingType)
                .mealBudget(this.mealBudget)
                .eatingHabit(this.eatingHabit)
                .distance(this.distance)
                .plusRequirement(this.plusRequirement)
                .restaurants(restaurants)
                .attractions(attractions)
                .cafes(cafes)
                .build();
    }

    public void update(Requirement requirement) {
        List<UUID> restaurantUUIDs = this.restaurants.stream().map(UUID::fromString).collect(Collectors.toList());
        List<UUID> attractionUUIDs = this.attractions.stream().map(UUID::fromString).collect(Collectors.toList());
        List<UUID> cafeUUIDs = this.cafes.stream().map(UUID::fromString).collect(Collectors.toList());

        requirement.update(
                this.style,
                this.schedule,
                this.lodgingCriteria,
                this.lodgingType,
                this.mealBudget,
                this.eatingHabit,
                this.distance,
                this.plusRequirement,
                restaurantUUIDs,
                attractionUUIDs,
                cafeUUIDs
        );
    }
}
