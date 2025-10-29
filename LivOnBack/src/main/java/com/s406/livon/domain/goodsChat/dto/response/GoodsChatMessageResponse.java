package com.s406.livon.domain.goodsChat.dto.response;


import com.s406.livon.domain.goodsChat.entity.GoodsChatMessage;
import com.s406.livon.domain.goodsChat.entity.GoodsChatPart;
import com.s406.livon.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@RequiredArgsConstructor
public class GoodsChatMessageResponse {

    private final Long chatMessageId;
    private final Long roomId;
    private final UUID senderId;
    private final String senderNickname;
    private final String message;
    private final String messageType;
//    private final String senderImageUrl;
    private final LocalDateTime sentAt;

    public static GoodsChatMessageResponse of(GoodsChatMessage chatMessage) {
        GoodsChatPart goodsChatPart = chatMessage.getGoodsChatPart();
        User sender = goodsChatPart.getUser();

        return GoodsChatMessageResponse.builder()
                .chatMessageId(chatMessage.getId())
                .roomId(goodsChatPart.getGoodsChatRoom().getId())
                .senderId(sender.getId())
                .senderNickname(sender.getNickname())
//                .senderImageUrl(sender.getImageUrl())
                .message(chatMessage.getContent())
                .messageType(chatMessage.getMessageType().getValue())
                .sentAt(chatMessage.getSentAt())
                .build();
    }
}
