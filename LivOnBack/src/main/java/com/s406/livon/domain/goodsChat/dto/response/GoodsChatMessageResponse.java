package com.s406.livon.domain.goodsChat.dto.response;


import com.s406.livon.domain.goodsChat.document.GoodsChatMessage;

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

    private final String chatMessageId;
    private final Long roomId;
    private final UUID senderId;
//    private final String senderNickname;
    private final String message;
    private final String messageType;
//    private final String senderImageUrl;
    private final LocalDateTime sentAt;

    public static GoodsChatMessageResponse of(GoodsChatMessage chatMessage, User sender) {
        return GoodsChatMessageResponse.builder()
                .chatMessageId(chatMessage.getId())
                .roomId(chatMessage.getChatRoomId())
                .senderId(sender.getId())
//                .senderNickname(sender.getNickname())
//                .senderImageUrl(FileUtils.getThumbnailImageUrl(sender.getImageUrl()))
                .message(chatMessage.getContent())
                .messageType(chatMessage.getMessageType().getValue())
                .sentAt(chatMessage.getSentAt())
                .build();
    }
}
