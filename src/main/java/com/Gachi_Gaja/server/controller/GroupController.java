package com.Gachi_Gaja.server.controller;

import com.Gachi_Gaja.server.dto.MemberInfoDTO;
import com.Gachi_Gaja.server.dto.request.GroupRequestDTO;
import com.Gachi_Gaja.server.dto.response.GroupListResponseDTO;
import com.Gachi_Gaja.server.dto.response.GroupResponseDTO;
import com.Gachi_Gaja.server.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    // 🔹 JWT에서 userId 추출하는 공통 메서드
    private UUID getUserIdFromJWT() {
        return (UUID) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    /** 1️. 모임 생성 (+리더 등록) */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createGroup(
            @Valid @RequestBody GroupRequestDTO dto) {

        UUID userId = getUserIdFromJWT();  // ★ JWT에서 userId 가져오기

        UUID groupId = groupService.createGroup(dto, userId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "그룹이 성공적으로 생성되었습니다.");
        response.put("groupId", groupId);

        return ResponseEntity.ok(response);
    }

    /** 2. 가입한 모임 전체 조회 */
    @GetMapping
    public ResponseEntity<GroupListResponseDTO> getGroups() {

        UUID userId = getUserIdFromJWT();  // ★ JWT 기반

        return ResponseEntity.ok(groupService.getGroupsByUser(userId));
    }

    /** 3. 모임 상세 조회 */
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponseDTO> getGroup(@PathVariable UUID groupId) {
        return ResponseEntity.ok(groupService.getGroupById(groupId));
    }

    /** 4. 모임 삭제 */
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Map<String, String>> deleteGroup(
            @PathVariable UUID groupId) {

        UUID userId = getUserIdFromJWT();  // ★ 리더 검증도 JWT로 수행

        groupService.deleteGroup(groupId, userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "그룹이 성공적으로 삭제되었습니다.");

        return ResponseEntity.ok(response);
    }

    /** 5. 모임 정보 수정 */
    @PutMapping("/{groupId}")
    public ResponseEntity<Map<String, Object>> updateGroup(
            @PathVariable UUID groupId,
            @Valid @RequestBody GroupRequestDTO dto) {

        UUID userId = getUserIdFromJWT();  // ★ JWT 기반

        groupService.updateGroup(groupId, userId, dto);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "그룹 정보가 성공적으로 변경되었습니다.");
        response.put("groupId", groupId);

        return ResponseEntity.ok(response);
    }

    /** 6. 모임 멤버 조회 */
    @GetMapping("/{groupId}/members")
    public ResponseEntity<Map<String, Object>> getGroupMembers(@PathVariable UUID groupId) {
        List<MemberInfoDTO> members = groupService.getGroupMembers(groupId);

        Map<String, Object> response = new HashMap<>();
        response.put("Members", members);

        return ResponseEntity.ok(response);
    }

    /** 7. 모임 멤버 추가 */
    @PostMapping("/{groupId}/members")
    public ResponseEntity<Map<String, Object>> addMemberToGroup(
            @PathVariable UUID groupId) {

        UUID userId = getUserIdFromJWT();  // ★ JWT 기반

        groupService.addMemberToGroup(groupId, userId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "유저가 그룹에 성공적으로 추가되었습니다.");
        response.put("groupId", groupId);

        return ResponseEntity.ok(response);
    }
}