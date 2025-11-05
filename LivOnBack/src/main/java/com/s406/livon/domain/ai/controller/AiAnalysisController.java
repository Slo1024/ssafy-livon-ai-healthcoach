package com.s406.livon.domain.ai.controller;

import com.s406.livon.domain.ai.dto.response.AiSummaryResponseDto;
import com.s406.livon.domain.ai.service.AiAnalysisService;
import com.s406.livon.global.security.jwt.JwtTokenProvider;
import com.s406.livon.global.web.response.ApiResponse;
import com.s406.livon.global.web.response.code.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiAnalysisController {

    private final AiAnalysisService aiAnalysisService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/health-summary")
    @Operation(summary = "AI 건강 요약 생성 API", description = "사용자의 건강 설문 데이터를 기반으로 AI 요약을 생성합니다.")
    public ResponseEntity<?> generateSummary(@RequestHeader("Authorization") String token) {
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));
        AiSummaryResponseDto response = aiAnalysisService.generateSummary(userId);
        return ResponseEntity.ok().body(ApiResponse.of(SuccessStatus.UPDATE_SUCCESS, response));
    }

    @GetMapping("/health-summary")
    @Operation(summary = "AI 건강 요약 조회 API", description = "생성된 AI 건강 요약을 조회합니다.")
    public ResponseEntity<?> getSummary(@RequestHeader("Authorization") String token) {
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));
        AiSummaryResponseDto response = aiAnalysisService.getSummary(userId);
        return ResponseEntity.ok().body(ApiResponse.of(SuccessStatus.SELECT_SUCCESS, response));
    }

}
