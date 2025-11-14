package com.Gachi_Gaja.server.controller;

import com.Gachi_Gaja.server.service.InviteService;
import jakarta.servlet.http.HttpSession;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class InviteController {

    private final InviteService inviteService;

    @PostMapping("/api/invites/{groupId}")
    public ResponseEntity<Void> joinGroup(@PathVariable UUID groupId, HttpSession httpSession) {

        UUID userId = (UUID) httpSession.getAttribute("userId");

        return inviteService.joinGroup(userId, groupId);
    }

}
