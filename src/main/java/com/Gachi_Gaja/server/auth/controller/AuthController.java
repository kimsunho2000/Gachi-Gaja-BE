package com.Gachi_Gaja.server.auth.controller;

import com.Gachi_Gaja.server.auth.service.UserService;
import com.Gachi_Gaja.server.dto.request.LoginRequestDTO;
import com.Gachi_Gaja.server.dto.request.UserRequestDTO;
import com.Gachi_Gaja.server.dto.response.UserResponseDTO;
import com.Gachi_Gaja.server.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    // 로그인
    @PostMapping("/api/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO request) {
        User user = userService.login(request);
        record LoginResponse(UUID userId, String message) {}
        return ResponseEntity.ok(new LoginResponse(user.getUserId(), "로그인이 완료되었습니다."));
    }

    // 로그아웃 (더미)
    @PostMapping("/api/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok().build();
    }

    // 회원가입
    @PostMapping("/api/users")
    public ResponseEntity<?> register(@Valid @RequestBody UserRequestDTO request) {
        User user = userService.register(request);
        record RegisterResponse(UUID userId, String message) {}
        return ResponseEntity.ok(new RegisterResponse(user.getUserId(), "회원가입이 완료되었습니다."));
    }

    // 내 정보 조회
    @GetMapping("/api/users/me")
    public ResponseEntity<UserResponseDTO> me(@RequestParam UUID userId) {
        User me = userService.getMe(userId);
        return ResponseEntity.ok(new UserResponseDTO(me.getUserId(), me.getNickname(), me.getEmail()));
    }

    // 내 정보 수정 (모든 필드 필수: 기존 UserRequestDTO 사용)
    @PutMapping("/api/users/me")
    public ResponseEntity<Void> update(@RequestParam UUID userId,
                                       @Valid @RequestBody UserRequestDTO request) {
        userService.updateMe(userId, request);
        return ResponseEntity.ok().build();
    }

    // 회원 탈퇴 (userId param + LoginRequestDTO body)
    @DeleteMapping("/api/users/me")
    public ResponseEntity<Void> delete(@RequestParam UUID userId) {
        userService.deleteMe(userId);
        return ResponseEntity.ok().build();
    }
}
