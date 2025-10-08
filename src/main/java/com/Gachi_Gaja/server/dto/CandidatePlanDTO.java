package com.Gachi_Gaja.server.dto;

import java.util.UUID;
import lombok.Builder;

@Builder
public record CandidatePlanDTO(
   UUID candidatePlanId,
   String plan,
   String reasons,
   int votes,
   boolean voted
) {}
