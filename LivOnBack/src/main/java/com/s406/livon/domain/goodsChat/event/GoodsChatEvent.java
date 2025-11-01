package com.s406.livon.domain.goodsChat.event;


import com.s406.livon.domain.goodsChat.entity.MessageType;
import com.s406.livon.domain.user.entity.User;

public record GoodsChatEvent(Long chatRoomId, User user, MessageType type) {

    public static GoodsChatEvent from(Long chatRoomId, User user, MessageType type) {
        return new GoodsChatEvent(chatRoomId, user, type);
    }
}
