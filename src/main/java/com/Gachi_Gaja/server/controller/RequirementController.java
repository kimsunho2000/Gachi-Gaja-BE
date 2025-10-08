package com.Gachi_Gaja.server.controller;

import com.Gachi_Gaja.server.dto.request.RequirementRequestDTO;
import com.Gachi_Gaja.server.dto.response.RequirementResponseDTO;
import com.Gachi_Gaja.server.repository.RequirementRepository;
import com.Gachi_Gaja.server.service.RequirementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class RequirementController {

    private final RequirementService requirementService;

    @PostMapping("/api/groups/{groupId}/requirements")
    public ResponseEntity<?> generateRequirement(@Validated @PathVariable UUID groupId, @ModelAttribute RequirementRequestDTO request) {


        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/groups/{groupId}/requirements/{requirementId}")
    public ResponseEntity<RequirementResponseDTO> getRequirement(@PathVariable UUID groupId, UUID requirementId) {

        requirementService.
        return ResponseEntity.ok().build();
    }

    @PutMapping("/api/groups/{groupId}/requirements/{requirementId}")
    public ResponseEntity<RequirementResponseDTO> putRequirement() {

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/groups/{groupId}/requirements/{requirementId}")
    public ResponseEntity<?> deleteRequirement(@PathVariable UUID groupId, UUID requirementId) {
        requirementService.deleteRequirement(groupId, requirementId);
        return ResponseEntity.ok().build();
    }

}
