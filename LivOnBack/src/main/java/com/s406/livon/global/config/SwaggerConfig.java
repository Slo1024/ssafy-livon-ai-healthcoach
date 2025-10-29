package com.s406.livon.global.config; // 본인의 패키지 경로에 맞게 수정하세요.

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        // API 문서의 기본 정보 설정
        Info info = new Info()
                .title("LivOn API Document") // API 제목
                .version("v0.0.1") // API 버전
                .description("LivOn 프로젝트의 API 명세서입니다."); // API 설명

        // JWT 인증 설정을 위한 SecurityScheme 정의
        String jwtSchemeName = "JWT Token";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
        
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP) // HTTP 방식
                        .scheme("bearer") // bearer 토큰 방식
                        .bearerFormat("JWT")); // 토큰 형식

        // OpenAPI 객체를 생성하여 정보와 보안 설정을 추가
        return new OpenAPI()
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}