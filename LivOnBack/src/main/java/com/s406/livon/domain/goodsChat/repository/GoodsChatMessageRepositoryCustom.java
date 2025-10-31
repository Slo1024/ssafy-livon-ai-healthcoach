package com.s406.livon.domain.goodsChat.repository;


import com.s406.livon.domain.goodsChat.document.GoodsChatMessage;

import java.time.LocalDateTime;
import java.util.List;

public interface GoodsChatMessageRepositoryCustom {
    List<GoodsChatMessage> getChatMessages(Long chatRoomId, LocalDateTime lastSentAt, int size);
}
