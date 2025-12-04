package com.Gachi_Gaja.server.configure;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfigure {

    @Bean
    public OpenAPI openAPI() {
        // JWT 보안 정의
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("JWT");

        // JWT 인증 방식 추가
        Components components = new Components().addSecuritySchemes("JWT", new SecurityScheme()
                .name("JWT")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT"));

        return new OpenAPI()
                .components(new Components())
                .info(apiInfo())
                .addSecurityItem(securityRequirement)
                .components(components);
    }

    private Info apiInfo() {
        return new Info()
                .title("같이가자 API TEST")
                .description("같이가자 API 테스트를 위한 Swagger 문서입니다.")
                .version("1.0.0");
    }

}