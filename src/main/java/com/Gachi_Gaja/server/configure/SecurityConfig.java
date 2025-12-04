package com.Gachi_Gaja.server.configure;

import com.Gachi_Gaja.server.jwt.JwtAuthenticationFilter;
import com.Gachi_Gaja.server.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // CORS 활성화 (WebConfigure의 설정 사용)
                .cors(cors -> {})

                .csrf(csrf -> csrf.disable())

                // 세션을 전혀 사용하지 않음 (JWT라 Stateless)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 기본 로그인/Basic Auth 전부 비활성화
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                .authorizeHttpRequests(auth -> auth
                        // JWT 없이 허용되는 요청들
                        .requestMatchers("/api/login", "/api/users", "/api/logout").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**",
                                "/swagger-resources/**", "/webjars/**").permitAll()

                        // 나머지 API는 인증 필요
                        .anyRequest().authenticated()
                )

                // 🔥 JWT 필터 추가: UsernamePasswordAuthenticationFilter BEFORE
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

