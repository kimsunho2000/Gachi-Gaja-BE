package com.Gachi_Gaja.server.controller;

import com.Gachi_Gaja.server.dto.response.CandidatePlanResponseDTO;
import com.Gachi_Gaja.server.service.CandidatePlanService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.LimitExceededException;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CandidatePlanController {

    private final CandidatePlanService candidatePlanService;

    /*
    여행 계획 후보 생성 메서드
     */
    @PostMapping("/api/groups/{groupId}/candidates")
    public ResponseEntity<?> generateCandidatePlan(@PathVariable UUID groupId, HttpSession session) throws LimitExceededException {
        UUID userId = (UUID) session.getAttribute("userId");

        candidatePlanService.generateCandidatePlan(groupId, userId);

        return ResponseEntity.ok().build();
    }

    /*
    여행 계획 후보 전체 조회 메서드
     */
    @GetMapping("/api/groups/{groupId}/candidates")
    public ResponseEntity<CandidatePlanResponseDTO> getCandidatePlans(@PathVariable UUID groupId, HttpSession session) {
        UUID userId = (UUID) session.getAttribute("userId");

        CandidatePlanResponseDTO candidatePlans = candidatePlanService.findByAll(groupId, userId);

        return ResponseEntity.ok().body(candidatePlans);
    }

    /*
    여행 계획 후보 단일 조회 메서드
    @GetMapping("/api/groups/{groupId}/candidates/{candidateId}")
    public ResponseEntity<CandidatePlanResponseDTO> getCandidatePlan(@PathVariable UUID groupId, UUID candidateId) {
        CandidatePlanResponseDTO response = candidatePlanService.findById(candidateId);

        return ResponseEntity.ok().body(response);
    }
     */

}
