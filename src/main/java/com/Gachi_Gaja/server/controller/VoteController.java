package com.Gachi_Gaja.server.controller;

import com.Gachi_Gaja.server.dto.request.VoteRequestDTO;
import com.Gachi_Gaja.server.dto.response.VoteResponseDTO;
import com.Gachi_Gaja.server.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/groups/{groupId}/candidates/votes")
public class VoteController {

    private final VoteService voteService;

    // JWT에서 userId 가져오는 공통 메서드
    private UUID getUserId() {
        return (UUID) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    // 투표 상태 조회
    @GetMapping
    public VoteResponseDTO getVote(@PathVariable UUID groupId) {

        UUID userId = getUserId();

        return voteService.getVote(groupId, userId);
    }

    // 투표 생성
    @PostMapping
    public String createVote(@PathVariable UUID groupId,
                             @RequestBody VoteRequestDTO request) {

        UUID userId = getUserId();

        voteService.createVote(groupId, userId, request);

        return "투표 완료";
    }

    // 투표 수정
    @PutMapping
    public String updateVote(@PathVariable UUID groupId,
                             @RequestBody VoteRequestDTO request) {

        UUID userId = getUserId();

        voteService.updateVote(groupId, userId, request);

        return "투표 수정 완료";
    }
}