package com.Gachi_Gaja.server.dto.response;

import com.Gachi_Gaja.server.domain.Member;
import com.Gachi_Gaja.server.domain.Requirement;
import com.Gachi_Gaja.server.domain.User;
import lombok.Builder;

import java.util.UUID;

@Builder
public record RequirementResponseDTO(

        UUID requirementId,
        UUID memberId,
        UUID userId,
        String nickName,
        String style,
        String schedule,
        String lodgingCriteria,
        String lodgingType,
        String mealBudget,
        String eatingHabit,
        String distance,
        String plusRequirement

) {
    public static RequirementResponseDTO from(Requirement r, User u, Member m) {
        return RequirementResponseDTO.builder()
                .requirementId(r.getRequirementId())
                .memberId(m.getMemberId())
                .userId(u.getUserId())
                .nickName(u.getNickname())
                .style(r.getStyle())
                .schedule(r.getSchedule())
                .lodgingCriteria(r.getLodgingCriteria())
                .lodgingType(r.getLodgingType())
                .mealBudget(r.getMealBudget())
                .eatingHabit(r.getEatingHabit())
                .distance(r.getDistance())
                .plusRequirement(r.getPlusRequirement())
                .build();
    }

    public static RequirementResponseDTO from(User u, Member m) {
        return RequirementResponseDTO.builder()
                .requirementId(null)
                .memberId(m.getMemberId())
                .userId(u.getUserId())
                .nickName(u.getNickname())
                .build();
    }
}
