package com.Gachi_Gaja.server.controller;

import com.Gachi_Gaja.server.dto.request.PlanRequestDTO;
import com.Gachi_Gaja.server.dto.response.PlanResponseDTO;
import com.Gachi_Gaja.server.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    // JWT 기반 userId 추출 메서드
    private UUID getUserId() {
        return (UUID) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    /*
    여행 계획 생성 메서드
     */
    @PostMapping("/api/groups/{groupId}/plans")
    public ResponseEntity<?> generatePlan(@PathVariable UUID groupId) {
        UUID userId = getUserId();

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
    */
    @PutMapping("/api/groups/{groupId}/plans/{planId}")
    public ResponseEntity<String> updatePlan(@Validated @RequestBody PlanRequestDTO request, @PathVariable UUID groupId, UUID planId) {
        UUID userId = getUserId();

        planService.update(groupId, planId, userId, request);

        return ResponseEntity.ok().body("여행 계획 수정이 완료되었습니다.");
    }
    
    /*
    여행 계획 삭제 메서드
     */
    @DeleteMapping("/api/groups/{groupId}/plans/{planId}")
    public ResponseEntity<String> deletePlan(@PathVariable UUID groupId, UUID planId) {
        UUID userId = getUserId();

        planService.delete(groupId, planId, userId);

        return ResponseEntity.ok().body("여행 계획 삭제가 완료되었습니다.");
    }
    
    /*
    여행 계획 추가 메서드
     */
    @PostMapping("/api/groups/{groupId}/new-plans")
    public ResponseEntity<String> createPlan(@Validated @RequestBody PlanRequestDTO request, @PathVariable UUID groupId) {
        UUID userId = getUserId();

        planService.add(groupId, userId, request);

        return ResponseEntity.ok().body("여행 계획 추가가 완료되었습니다.");
    }

}
