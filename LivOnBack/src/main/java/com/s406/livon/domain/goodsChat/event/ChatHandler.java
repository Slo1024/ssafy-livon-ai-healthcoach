package com.s406.livon.domain.goodsChat.event;


import com.s406.livon.domain.goodsChat.repository.GoodsChatPartRepository;
import com.s406.livon.domain.user.entity.User;
import com.s406.livon.global.error.handler.TokenHandler;
import com.s406.livon.global.security.jwt.JwtTokenProvider;
import com.s406.livon.global.web.response.code.ErrorReasonDTO;
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@RequiredArgsConstructor
@Slf4j
@Component
public class ChatHandler implements ChannelInterceptor {

    private final JwtTokenProvider tokenProvider;
    private final GoodsChatPartRepository goodsChatPartRepository;

    private final Map<String, UUID> userBySessionId = new ConcurrentHashMap<>();
    private final Map<Long, Set<String>> sessionsByRoomId = new ConcurrentHashMap<>();

    public Set<UUID> getConnectedUsers(Long roomId) {
        // 1. 채팅방에 구독 중인 세션 ID 목록을 가져옵니다.
        Set<String> sessionIds = sessionsByRoomId.getOrDefault(roomId, Collections.emptySet());

        // 2. 세션 ID 목록을 실제 사용자 ID(UUID) 목록으로 변환합니다.
        return sessionIds.stream()
                .map(userBySessionId::get) // 세션ID -> 유저ID
                .filter(Objects::nonNull)    // DISCONNECT 처리 중인 유저 제외
                .collect(Collectors.toSet());
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        //todo : 구독하는 것도 예외처리 이 채팅방으 구독 할 수 있는가?


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

                // 6. STOMP 세션에 사용자 정보(Authentication) 저장
                accessor.setUser(auth);
                String sessionId = accessor.getSessionId();
                UUID userId = ((User) auth.getPrincipal()).getId();
                userBySessionId.put(sessionId, userId);
                log.info("STOMP CONNECT: 세션 등록. SessionID: {}, UserID: {}", sessionId, userId);

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
        }else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            try {
                // (1) 사용자 정보 가져오기
                Authentication auth = (Authentication) accessor.getUser();
                if (auth == null || !(auth.getPrincipal() instanceof User)) {
                    log.warn("STOMP SUBSCRIBE: 인증되지 않은 사용자입니다.");
                    throw new AccessDeniedException("사용자 정보가 없습니다.");
                }
                User user = (User) auth.getPrincipal();

                // (2) 목적지(채팅방) 정보 가져오기
                String destination = accessor.getDestination();
                if (destination == null) { /* ... */ }

                // (3) 목적지에서 채팅방 ID 추출
                Long roomId = parseRoomIdFromDestination(destination);

                // ★ 4. 권한 검사 (가장 중요) ★
                // GoodsChatPart 테이블을 조회하여 이 사용자가 이 채팅방의 참여자인지 확인
                boolean isParticipant = goodsChatPartRepository.existsByUserIdAndGoodsChatRoomId(user.getId(), roomId);

                // (5) 참여자가 아닐 경우 접근 거부
                if (!isParticipant) {
                    log.warn("STOMP SUBSCRIBE: 접근 거부. User {} -> Room {}", user.getId(), roomId);
                    throw new AccessDeniedException("해당 채팅방에 접근할 권한이 없습니다.");
                }

                String sessionId = accessor.getSessionId();
                sessionsByRoomId.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);



                log.info("STOMP SUBSCRIBE 승인: User {} -> Room {}", user.getId(), roomId);

            } catch (Exception e) {
                log.error("STOMP SUBSCRIBE 인가 실패: {}", e.getMessage(), e);
                throw new AccessDeniedException("구독 인가에 실패했습니다.", e);
            }
        }else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            String sessionId = accessor.getSessionId();
            if (sessionId == null) {
                return message;
            }

            // (1) userBySessionId 맵에서 사용자 제거
            UUID userId = userBySessionId.remove(sessionId);

            if (userId != null) {
                log.info("STOMP DISCONNECT: 세션 종료. SessionID: {}, UserID: {}", sessionId, userId);

                // (2) sessionsByRoomId 맵을 순회하며 이 세션을 모든 방에서 제거
                sessionsByRoomId.values().forEach(sessions -> sessions.remove(sessionId));
            } else {
                log.debug("STOMP DISCONNECT: 추적되지 않는 세션 종료. SessionID: {}", sessionId);
            }
        }
        return message;
    }
    private Long parseRoomIdFromDestination(String destination) {
        // WebSocketConfig에서 설정한 prefix가 "/sub/chat/goods/"
        try {
            String[] parts = destination.split("/");
            return Long.parseLong(parts[parts.length - 1]);
        } catch (Exception e) {
            log.error("채팅방 ID 파싱 실패: {}", destination, e);
            throw new IllegalArgumentException("유효하지 않은 채팅방 구독 주소입니다.");
        }
    }
}