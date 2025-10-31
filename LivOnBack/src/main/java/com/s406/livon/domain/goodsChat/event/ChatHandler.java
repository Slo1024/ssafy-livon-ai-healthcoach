package com.s406.livon.domain.goodsChat.event;


import com.s406.livon.global.error.handler.TokenHandler;
import com.s406.livon.global.security.jwt.JwtTokenProvider;
import com.s406.livon.global.web.response.code.ErrorReasonDTO;
import com.s406.livon.global.web.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@RequiredArgsConstructor
@Slf4j
@Component
public class ChatHandler implements ChannelInterceptor {

    private final JwtTokenProvider tokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // STOMP CONNECT 요청일 때만 인증 처리
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            // 1. 예외 처리를 위한 try-catch 블록
            try {
                // 2. 헤더에서 "Authorization" 값을 List<String>으로 가져옴
                List<String> authHeaders = accessor.getNativeHeader("Authorization");

                // 헤더가 없으면 예외 처리
                if (authHeaders == null || authHeaders.isEmpty()) {
                    log.warn("STOMP CONNECT: Authorization 헤더가 없습니다.");
                    throw new AccessDeniedException("Authorization 헤더가 없습니다.");
                }

                // 3. "Bearer " 접두사 처리
                String bearerToken = authHeaders.get(0); // 첫 번째 헤더 값을 사용
                String token = null;

                if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                    token = bearerToken.substring(7); // "Bearer " (7글자) 제거
                } else {
                    log.warn("STOMP CONNECT: 유효하지 않은 Bearer 토큰 형식입니다.");
                    throw new AccessDeniedException("유효하지 않은 Bearer 토큰 형식입니다.");
                }

                // 토큰이 비어있는지 확인
                if (token == null || token.isEmpty()) {
                    log.warn("STOMP CONNECT: 토큰이 비어있습니다.");
                    throw new AccessDeniedException("토큰이 비어있습니다.");
                }

                log.info("로그인정보 (토큰): " + token);

                // 4. 토큰 검증 (실패 시 TokenHandler 예외 발생)
                tokenProvider.validateToken(token);

                // 5. 검증 성공 시, Authentication 객체 생성
                Authentication auth = tokenProvider.getAuthentication(token);

                System.out.println("응???" + auth);

                // 6. STOMP 세션에 사용자 정보(Authentication) 저장
                accessor.setUser(auth);
                log.info("STOMP CONNECT 인증 성공: {}", auth.getName());

            }
            // 7. TokenHandler 예외 처리 (validateToken 실패 시)
            catch (TokenHandler e) {
                ErrorReasonDTO errorStatus = e.getErrorReason();
                String errorMessage = (errorStatus != null) ? errorStatus.getMessage() : "유효하지 않은 토큰입니다.";

                // "null" 대신 구체적인 에러 메시지 로깅
                log.error("STOMP CONNECT 인증 실패: [Token Error] {}", errorMessage, e);

                // 예외를 던져 STOMP 연결을 거부
                throw new AccessDeniedException("STOMP 인증 실패: " + errorMessage, e);

            }
            // 8. 그 외 모든 예외 처리
            catch (Exception e) {
                log.error("STOMP CONNECT 인증 실패: [General Error] {}", e.getMessage(), e);
                throw new AccessDeniedException("STOMP 인증에 실패했습니다.", e);
            }
        }
        return message;
    }
}