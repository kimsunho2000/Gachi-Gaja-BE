package com.Gachi_Gaja.server.auth.service;

import com.Gachi_Gaja.server.auth.exception.*;
import com.Gachi_Gaja.server.auth.repository.UserRepository;
import com.Gachi_Gaja.server.domain.User;
import com.Gachi_Gaja.server.dto.request.LoginRequestDTO;
import com.Gachi_Gaja.server.dto.request.UserRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    public User register(UserRequestDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail()))
            throw new EmailAlreadyUsedException("이미 존재하는 이메일입니다.");

        User user = User.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .nickname(dto.getNickName())
                .build();
        return userRepository.save(user);
    }

    // 로그인
    public User login(LoginRequestDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new NotFoundException("해당 이메일을 찾을 수 없습니다."));
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword()))
            throw new WrongPasswordException("비밀번호가 일치하지 않습니다.");
        return user;
    }

    // 내 정보 조회 (임시: userId 직접 받음. JWT 붙이면 토큰에서 꺼내기)
    public User getMe(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
    }

    // 내 정보 수정 (비번/닉네임만)
    public void updateMe(UUID userId, UserRequestDTO dto) {
        User user = getMe(userId);

        if (userRepository.existsByEmailAndUserIdNot(dto.getEmail(), userId))
            throw new EmailAlreadyUsedException("이미 사용 중인 이메일입니다.");

        user.setEmail(dto.getEmail());
        user.update(passwordEncoder.encode(dto.getPassword()), dto.getNickName());
        userRepository.save(user);
    }

    // 회원 탈퇴
    public void deleteMe(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        userRepository.delete(user);
    }
}
