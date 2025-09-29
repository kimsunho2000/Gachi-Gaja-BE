package com.Gachi_Gaja.server.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/*
   투표 요청시 DTO
 */
public class VoteRequestDTO {

    @NotBlank
    String candidatePlanId;
}
