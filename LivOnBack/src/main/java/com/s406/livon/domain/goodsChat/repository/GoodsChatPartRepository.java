package com.s406.livon.domain.goodsChat.repository;

import com.s406.livon.domain.goodsChat.entity.GoodsChatPart;
import com.s406.livon.domain.goodsChat.entity.GoodsChatPartId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GoodsChatPartRepository extends JpaRepository<GoodsChatPart, GoodsChatPartId> {

    @Query("""
            SELECT cr
            FROM GoodsChatPart cr
            JOIN FETCH cr.user crm
            WHERE cr.goodsChatRoom.id = :chatRoomId
            AND cr.isActive = true
            """)
    List<GoodsChatPart> findAllWithMemberByChatRoomId(@Param("chatRoomId") Long chatRoomId);
}