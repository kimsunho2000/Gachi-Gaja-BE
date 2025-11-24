package com.Gachi_Gaja.server.controller;

import com.Gachi_Gaja.server.dto.request.RequirementRequestDTO;
import com.Gachi_Gaja.server.dto.response.RequirementResponseDTO;
import com.Gachi_Gaja.server.dto.response.TotalRequirementResponseDTO;
import com.Gachi_Gaja.server.service.RequirementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RequirementController {

    private final RequirementService requirementService;

    // JWT 기반 userId 추출 메서드
    private UUID getUserId() {
        return (UUID) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    @PostMapping("/api/groups/{groupId}/requirements")
    public ResponseEntity<RequirementResponseDTO> generateRequirement(@Validated @RequestBody RequirementRequestDTO request, @PathVariable UUID groupId) {

        UUID userId = getUserId();
        RequirementResponseDTO response = requirementService.generateRequirement(userId, groupId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/groups/{groupId}/requirements")
    public ResponseEntity<TotalRequirementResponseDTO> getRequirement(@PathVariable UUID groupId) {
        TotalRequirementResponseDTO totalRequirementResponseDTO = requirementService.getRequirement(groupId);
        return ResponseEntity.ok(totalRequirementResponseDTO);
    }

    @PutMapping("/api/groups/{groupId}/requirements/{requirementId}")
    public ResponseEntity<RequirementResponseDTO> putRequirement(@Validated @RequestBody RequirementRequestDTO request, @PathVariable UUID groupId, @PathVariable UUID requirementId) {

        UUID userId = getUserId();
        RequirementResponseDTO response = requirementService.putRequirement(userId, groupId, requirementId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/groups/{groupId}/requirements/{requirementId}")
    public ResponseEntity<?> deleteRequirement(@PathVariable UUID groupId,@PathVariable UUID requirementId) {
        log.info("result: {}, {}", groupId, requirementId);
        requirementService.deleteRequirement(groupId, requirementId);
        return ResponseEntity.ok().build();
    }

}
