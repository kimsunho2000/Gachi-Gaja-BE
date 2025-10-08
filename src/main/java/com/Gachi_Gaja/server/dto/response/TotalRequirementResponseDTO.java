package com.Gachi_Gaja.server.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record TotalRequirementResponseDTO(
        int totalMembers,
        List<RequirementResponseDTO> requirements
) {
    public static TotalRequirementResponseDTO from(int totalMembers, List<RequirementResponseDTO> requirements) {
        return new TotalRequirementResponseDTO(totalMembers, requirements == null ? List.of() : requirements);
    }
}
