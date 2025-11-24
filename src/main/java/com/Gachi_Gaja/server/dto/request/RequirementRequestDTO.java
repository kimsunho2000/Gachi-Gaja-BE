package com.Gachi_Gaja.server.dto.request;

import com.Gachi_Gaja.server.domain.Group;
import com.Gachi_Gaja.server.domain.Requirement;
import com.Gachi_Gaja.server.domain.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;

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


    public Requirement toEntity(Group group, User user) {
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
                .build();
    }

    public void update(Requirement requirement) {

        requirement.update(
                this.style,
                this.schedule,
                this.lodgingCriteria,
                this.lodgingType,
                this.mealBudget,
                this.eatingHabit,
                this.distance,
                this.plusRequirement
        );
    }
}
