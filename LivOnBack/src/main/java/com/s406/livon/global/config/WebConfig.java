//package com.s406.livon.global.config; // 👈 본인의 config 패키지 경로로 수정
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.method.HandlerTypePredicate;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration
//public class WebConfig implements WebMvcConfigurer {
//
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/api/**") // 👈 /api/v1/.. 등을 포함하도록 /api/** 설정
//                .allowedOrigins("*")   // 👈 모든 출처(file://, http://... 등)를 허용
//                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
//                .allowedHeaders("*")
//                .allowCredentials(false) // 👈 자격 증명(쿠키 등)을 사용하지 않는 경우
//                .maxAge(3600);
//    }
//}