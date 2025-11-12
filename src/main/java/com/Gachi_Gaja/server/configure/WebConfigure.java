package com.Gachi_Gaja.server.configure;

import com.Gachi_Gaja.server.configure.interceptor.LoginCheckInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfigure implements WebMvcConfigurer {

    private final LoginCheckInterceptor loginCheckInterceptor;

    /*
        CORS 설정
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:8080") // 추후 프론트 URL 추가
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /*
       인터셉터 추가
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginCheckInterceptor)
                .addPathPatterns("/api/**") // 보호 경로
                .excludePathPatterns(
                        "/api/login", // 로그인 허용
                        "/api/users", // 회원가입 허용
                        "/error",
                        "/favicon.ico"
                );
    }
}
