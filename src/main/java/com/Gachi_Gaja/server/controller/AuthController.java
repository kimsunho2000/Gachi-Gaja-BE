package com.Gachi_Gaja.server.controller;

import com.Gachi_Gaja.server.jwt.JwtTokenProvider;
import com.Gachi_Gaja.server.service.UserService;
import com.Gachi_Gaja.server.dto.request.LoginRequestDTO;
import com.Gachi_Gaja.server.dto.request.UserRequestDTO;
import com.Gachi_Gaja.server.dto.response.UserResponseDTO;
import com.Gachi_Gaja.server.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    // 🔹 로그인 (JWT 토큰 발급)
    @PostMapping("/api/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO request) {
        User user = userService.login(request);

        // ★ 기존 구조 유지하면서 accessToken 추가
        record LoginResponse(UUID userId, String accessToken, String message) {}

        String token = jwtTokenProvider.generateToken(user.getUserId());

        return ResponseEntity.ok(new LoginResponse(
                user.getUserId(),
                token,
                "로그인이 완료되었습니다."
        ));
    }

    // 🔹 로그아웃 (JWT는 서버 상태 없으므로 그대로 둠)
    @PostMapping("/api/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok().build();
    }

    // 🔹 회원가입 (기존 그대로)
    @PostMapping("/api/users")
    public ResponseEntity<?> register(@Valid @RequestBody UserRequestDTO request) {
        User user = userService.register(request);
        record RegisterResponse(UUID userId, String message) {}
        return ResponseEntity.ok(new RegisterResponse(
                user.getUserId(),
                "회원가입이 완료되었습니다."
        ));
    }

    // 🔹 내 정보 조회 (JWT에서 userId 추출)
    @GetMapping("/api/users/me")
    public ResponseEntity<UserResponseDTO> me() {

        UUID userId = (UUID) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User me = userService.getMe(userId);
        return ResponseEntity.ok(new UserResponseDTO(
                me.getUserId(),
                me.getNickname(),
                me.getEmail()
        ));
    }

    // 🔹 내 정보 수정
    @PutMapping("/api/users/me")
    public ResponseEntity<Void> update(@Valid @RequestBody UserRequestDTO request) {

        UUID userId = (UUID) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        userService.updateMe(userId, request);
        return ResponseEntity.ok().build();
    }

    // 🔹 회원 탈퇴
    @DeleteMapping("/api/users/me")
    public ResponseEntity<Void> delete() {

        UUID userId = (UUID) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        userService.deleteMe(userId);
        return ResponseEntity.ok().build();
    }
}