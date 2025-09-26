package com.Gachi_Gaja.server.dto;

public record CandidatePlanDTO(
   String candidatePlanId,
   String plan,
   String reasons,
   int votes,
   boolean voted
) {}
