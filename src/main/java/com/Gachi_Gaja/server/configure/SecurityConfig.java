package com.Gachi_Gaja.server.configure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // REST API니까 CSRF 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/login", "/api/users").permitAll() // 로그인, 회원가입 허용
                        .anyRequest().permitAll() // JWT 적용 전까진 임시로 모두 허용
                );

        return http.build();
    }
}