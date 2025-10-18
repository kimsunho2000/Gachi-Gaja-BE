package com.Gachi_Gaja.server.controller;

import com.Gachi_Gaja.server.dto.response.PlanResponseDTO;
import com.Gachi_Gaja.server.service.GeminiService;
import com.Gachi_Gaja.server.service.PlanService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    /*
    여행 계획 생성 메서드
     */
    @PostMapping("/api/groups/{groupId}/plans")
    public ResponseEntity<?> generatePlan(@PathVariable UUID groupId, HttpSession session) {
        UUID userId = (UUID) session.getAttribute("userId");

        planService.generatePlan(groupId, userId);

        return ResponseEntity.ok().build();
    }

    /*
    여행 계획 전체 조회 메서드
     */
    @GetMapping("/api/groups/{groupId}/plans")
    public ResponseEntity<PlanResponseDTO> getPlan(@PathVariable UUID groupId) {
        PlanResponseDTO plans = planService.findAll(groupId);

        return ResponseEntity.ok().body(plans);
    }

    /*
    여행 계획 수정 메서드
    @PutMapping("/api/groups/{groupId}/plans/{planId}")
    public ResponseEntity<?> updatePlan() {
        return ResponseEntity.ok().build();
    }
     */

}
