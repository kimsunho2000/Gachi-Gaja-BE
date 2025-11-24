package com.Gachi_Gaja.server.controller;

import com.Gachi_Gaja.server.dto.request.RequirementRequestDTO;
import com.Gachi_Gaja.server.dto.response.RequirementResponseDTO;
import com.Gachi_Gaja.server.dto.response.TotalRequirementResponseDTO;
import com.Gachi_Gaja.server.service.RequirementService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RequirementController {

    private final RequirementService requirementService;

    @PostMapping("/api/groups/{groupId}/requirements")
    public ResponseEntity<RequirementResponseDTO> generateRequirement(@Validated @RequestBody RequirementRequestDTO request, @PathVariable UUID groupId, HttpSession httpSession) {

        UUID userId = (UUID) httpSession.getAttribute("userId");
        RequirementResponseDTO response = requirementService.generateRequirement(userId, groupId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/groups/{groupId}/requirements")
    public ResponseEntity<TotalRequirementResponseDTO> getRequirement(@PathVariable UUID groupId) {
        TotalRequirementResponseDTO totalRequirementResponseDTO = requirementService.getRequirement(groupId);
        return ResponseEntity.ok(totalRequirementResponseDTO);
    }

    @PutMapping("/api/groups/{groupId}/requirements/{requirementId}")
    public ResponseEntity<RequirementResponseDTO> putRequirement(@Validated @RequestBody RequirementRequestDTO request, @PathVariable UUID groupId,@PathVariable UUID requirementId, HttpSession httpSession) {

        UUID userId = (UUID) httpSession.getAttribute("userId");
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
