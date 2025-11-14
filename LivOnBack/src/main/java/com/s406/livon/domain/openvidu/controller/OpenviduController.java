package com.s406.livon.domain.openvidu.controller;

import java.util.Map;
import java.util.UUID;
import com.s406.livon.domain.openvidu.dto.request.TokenRequestDto;
import com.s406.livon.domain.openvidu.service.OpenviduService;
import com.s406.livon.global.security.jwt.JwtTokenProvider;
import com.s406.livon.global.web.response.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.livekit.server.WebhookReceiver;
import livekit.LivekitWebhook.WebhookEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@CrossOrigin(origins = "*")
@RestController
@RequiredArgsConstructor
@Slf4j
public class OpenviduController {

    private final OpenviduService openviduService;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${livekit.api.key}")
    private String LIVEKIT_API_KEY;

    @Value("${livekit.api.secret}")
    private String LIVEKIT_API_SECRET;

    /**
     * LiveKit 토큰 발급 API
     * - JWT 토큰에서 사용자 인증
     * - consultationId로 세션 조회 및 권한 검증
     * - LiveKit 접속 토큰 발급
     *
     * @param token Authorization 헤더 (Bearer {token})
     * @param request consultationId, participantName(선택)
     * @return LiveKit 접속 토큰
     */
    @PostMapping(value = "/token")
    public ResponseEntity<ApiResponse<Map<String, String>>> createToken(
            @RequestHeader("Authorization") String token,
            @RequestBody TokenRequestDto request
    ) {
        log.info("토큰 발급 요청 - consultationId: {}, participantName: {}",
                request.getConsultationId(), request.getParticipantName());

        // 1. JWT 토큰에서 userId 추출
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));

        // 2. 토큰 생성 (권한 검증 포함)
        String livekitToken = openviduService.createToken(
                userId,
                request.getConsultationId(),
                request.getParticipantName()
        );

        // 3. 응답 반환
        return ResponseEntity.ok(
                ApiResponse.onSuccess(Map.of("token", livekitToken))
        );
    }

    /**
     * LiveKit Webhook 수신
     * - 방 생성, 참가자 입퇴장, 녹화 등 이벤트 처리
     */
    @PostMapping(value = "/livekit/webhook", consumes = "application/webhook+json")
    public ResponseEntity<String> receiveWebhook(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody String body
    ) {
        WebhookReceiver webhookReceiver = new WebhookReceiver(LIVEKIT_API_KEY, LIVEKIT_API_SECRET);
        try {
            WebhookEvent event = webhookReceiver.receive(body, authHeader);
            log.info("LiveKit Webhook 수신: {}", event.toString());

            // TODO: 이벤트 타입별 처리 로직 추가
            // - RoomStarted: 상담 시작 시간 기록
            // - RoomFinished: 상담 종료 시간 기록
            // - ParticipantJoined: 참가자 입장 로그
            // - ParticipantLeft: 참가자 퇴장 로그

        } catch (Exception e) {
            log.error("LiveKit Webhook 검증 오류: {}", e.getMessage(), e);
        }
        return ResponseEntity.ok("ok");
    }
}