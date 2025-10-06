package com.Gachi_Gaja.server.controller;

import com.Gachi_Gaja.server.service.CandidatePlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CandidatePlanController {

    private final CandidatePlanService candidatePlanService;

    /*
    여행 계획 후보 생성 메서드
     */
    @PostMapping("/api/groups/{groupId}/candidates")
    public ResponseEntity<?> generateCandidatePlan(@PathVariable UUID groupId) {
        return ResponseEntity.ok().build();
    }

    /*
    여행 계획 생성 테스므 메서드 (추후 삭제)
     */
    /*
    @PostMapping("api/test/candidates")
    public ResponseEntity<String> generateCandidatePlanTest() {
        String candidatePlan = candidatePlanService.generateCandidatePlanTest();

        return ResponseEntity.ok().body(candidatePlan);
    }
     */

}
