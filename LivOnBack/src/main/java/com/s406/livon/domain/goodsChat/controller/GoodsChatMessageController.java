package com.s406.livon.domain.goodsChat.controller;


import com.s406.livon.domain.goodsChat.dto.request.GoodsChatMessageRequest;
import com.s406.livon.domain.goodsChat.service.GoodsChatMessageService;
import com.s406.livon.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class GoodsChatMessageController {

    private final GoodsChatMessageService goodsChatMessageService;

    @MessageMapping("/chat/goods/message")
    public void handleMessage(Authentication authentication, @Payload GoodsChatMessageRequest message) {
//        org.springframework.security.core.userdetails.User user =
//                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

        User sender = (User) authentication.getPrincipal();
        System.out.println(sender.getNickname());
        System.out.println(sender.getPassword());
//        System.out.println(sender.getNickname());
//        log.info("User: {}", user);

        goodsChatMessageService.sendMessage(message,sender);
    }
}
