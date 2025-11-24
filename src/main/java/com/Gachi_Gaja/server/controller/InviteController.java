package com.Gachi_Gaja.server.controller;

import com.Gachi_Gaja.server.service.InviteService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequiredArgsConstructor
@Slf4j
public class InviteController {

    private final InviteService inviteService;

    @PostMapping("/api/invites/{groupId}")
    public ResponseEntity<Void> joinGroup(@PathVariable UUID groupId) {

        UUID userId = (UUID) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        return inviteService.joinGroup(userId, groupId);
    }

}
