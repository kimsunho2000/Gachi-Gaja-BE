package com.Gachi_Gaja.server.dto.response;

import com.Gachi_Gaja.server.dto.CandidatePlanDTO;

import java.time.LocalDate;
import java.util.List;

public record VoteResponseDTO(
        boolean leaderVoted,
        boolean isLeader,
        LocalDate deadline,
        List<CandidatePlanDTO> candidatePlanList
) {}
