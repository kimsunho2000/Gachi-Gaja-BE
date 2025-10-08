package com.Gachi_Gaja.server.controller;

import com.Gachi_Gaja.server.dto.request.RequirementRequestDTO;
import com.Gachi_Gaja.server.dto.response.RequirementResponseDTO;
import com.Gachi_Gaja.server.dto.response.TotalRequirementResponseDTO;
import com.Gachi_Gaja.server.service.MockService;
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
    private final MockService mockService;

    @PostMapping("/api/groups/{groupId}/requirements")
    public ResponseEntity<RequirementResponseDTO> generateRequirement(@Validated @RequestBody RequirementRequestDTO request, @PathVariable UUID groupId, HttpSession httpSession) {

        UUID userId = (UUID) httpSession.getAttribute("userId");

        // 테스트를 위한 mock User,Group 생성
//        User testUser = mockService.createMockUser(1);
//        UUID testUserId = testUser.getUserId();
//        Group testGroup = mockService.createMockGroup();
//        UUID testGroupId = testGroup.getGroupId();
//        mockService.createMockMember(testUser, testGroup);
//        RequirementResponseDTO response = requirementService.generateRequirement(testUserId, testGroupId, request);

        // 정상 비즈니스 로직
        RequirementResponseDTO response = requirementService.generateRequirement(userId, groupId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/groups/{groupId}/requirements")
    public ResponseEntity<TotalRequirementResponseDTO> getRequirement(@PathVariable UUID groupId) {

        // 테스트 코드
//        List<UUID> testId = mockService.generateMockIds();
//        UUID testGroupId = testId.get(1);
//        TotalRequirementResponseDTO totalRequirementResponseDTO = requirementService.getRequirement(testGroupId);

        // 정상 비즈니스 로직
        TotalRequirementResponseDTO totalRequirementResponseDTO = requirementService.getRequirement(groupId);
        return ResponseEntity.ok(totalRequirementResponseDTO);
    }

    @PutMapping("/api/groups/{groupId}/requirements/{requirementId}")
    public ResponseEntity<RequirementResponseDTO> putRequirement(@Validated @RequestBody RequirementRequestDTO request, @PathVariable UUID groupId, UUID requirementId, HttpSession httpSession) {

        UUID userId = (UUID) httpSession.getAttribute("userId");

        // 테스트 코드
//        List<UUID> testId = mockService.generateMockId();
//        UUID testUserId = testId.get(0);
//        UUID testGroupId = testId.get(1);
//        UUID testRequirementId = testId.get(2);
//        RequirementResponseDTO response = requirementService.putRequirement(testUserId,testGroupId, testRequirementId, request);

        // 정상 비즈니스 로직
        RequirementResponseDTO response = requirementService.putRequirement(userId, groupId, requirementId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/groups/{groupId}/requirements/{requirementId}")
    public ResponseEntity<?> deleteRequirement(@PathVariable UUID groupId, UUID requirementId) {

        //테스트 코드
//        List<UUID> testId = mockService.generateMockId();
//        UUID testUserId = testId.get(0);
//        UUID testGroupId = testId.get(1);
//        UUID testRequirementId = testId.get(2);
//        requirementService.deleteRequirement(testGroupId, testRequirementId);

        //정상 비즈니스 로직
        requirementService.deleteRequirement(groupId, requirementId);
        return ResponseEntity.ok().build();
    }

}
