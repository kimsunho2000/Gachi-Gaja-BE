package com.Gachi_Gaja.server.controller;

import com.Gachi_Gaja.server.service.GeminiService;
import com.Gachi_Gaja.server.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    /*
    여행 계획 생성 메서드
     */
    @PostMapping("/api/groups/{groupId}/plans")
    public ResponseEntity<?> generateCandidatePlan(@PathVariable UUID groupId) {
        return ResponseEntity.ok().build();
    }

    /*
   여행 계획 생성 테스트 메서드 (추후 삭제)
    */
    /*
    @PostMapping("api/test/plans")
    public ResponseEntity<String> generateCandidatePlanTest() {
        String plan = planService.generatePlanTest();

        return ResponseEntity.ok().body(plan);
    }
     */

}
