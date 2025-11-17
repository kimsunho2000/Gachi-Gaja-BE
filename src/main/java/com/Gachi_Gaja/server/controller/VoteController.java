package com.Gachi_Gaja.server.controller;

import com.Gachi_Gaja.server.dto.request.VoteRequestDTO;
import com.Gachi_Gaja.server.dto.response.VoteResponseDTO;
import com.Gachi_Gaja.server.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/groups/{groupId}/candidates/votes")
public class VoteController {

    private final VoteService voteService;

    // 투표 상태 조회
    @GetMapping
    public VoteResponseDTO getVote(@PathVariable UUID groupId,
                                   @RequestParam UUID userId) {
        return voteService.getVote(groupId, userId);
    }

    // 투표 생성
    @PostMapping
    public String createVote(@PathVariable UUID groupId,
                             @RequestParam UUID userId,
                             @RequestBody VoteRequestDTO request) {

        voteService.createVote(groupId, userId, request);
        return "투표 완료";
    }

    // 투표 수정
    @PutMapping
    public String updateVote(@PathVariable UUID groupId,
                             @RequestParam UUID userId,
                             @RequestBody VoteRequestDTO request) {

        voteService.updateVote(groupId, userId, request);
        return "투표 수정 완료";
    }
}