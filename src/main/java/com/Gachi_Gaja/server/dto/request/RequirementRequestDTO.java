package com.Gachi_Gaja.server.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

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

    /*
      각 객체별 UUID 리스트
     */
    @NotEmpty
    List<String> restaurants;

    @NotEmpty
    List<String> attractions;

    @NotEmpty
    List<String> cafes;

}
