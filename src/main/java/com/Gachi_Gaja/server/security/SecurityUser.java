package com.Gachi_Gaja.server.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * JWT 에서 꺼낸 userId(UUID)를 담는 Security 전용 User 객체
 */
public class SecurityUser implements UserDetails {

    private final String userId;  // UUID 문자열

    public SecurityUser(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    // == UserDetails 구현부 ==

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 현재는 권한 개념 사용 안 하므로 빈 리스트
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        // JWT 기반이므로 의미 없음
        return "";
    }

    @Override
    public String getUsername() {
        // username 자리에 userId(UUID 문자열)를 넣어둠
        return userId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}