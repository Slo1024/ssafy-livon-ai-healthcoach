package com.s406.livon.domain.goodsChat.repository;

import com.s406.livon.domain.goodsChat.document.GoodsChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface GoodsChatMessageRepository
        extends MongoRepository<GoodsChatMessage, String>, GoodsChatMessageRepositoryCustom {

    /**
     * 특정 채팅방에 속한 모든 메시지를 삭제합니다.
     * @param chatRoomId 삭제할 채팅방 ID
     */
    void deleteAllByChatRoomId(Long chatRoomId);
}

