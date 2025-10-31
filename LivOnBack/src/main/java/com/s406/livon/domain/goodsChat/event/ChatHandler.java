package com.s406.livon.domain.goodsChat.event;


import com.s406.livon.global.security.jwt.JwtTokenProvider;
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
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

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

        System.out.println(accessor.getCommand());
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = String.valueOf(accessor.getNativeHeader("Authorization"));
            System.out.println(token);
            log.info("로그인정보"+token.substring(1, token.length() - 1));

            Authentication auth = tokenProvider.getAuthentication(token.substring(1, token.length() - 1));
            System.out.println("응???"+auth);
            accessor.setUser(auth);

        }
        return message;
    }
}