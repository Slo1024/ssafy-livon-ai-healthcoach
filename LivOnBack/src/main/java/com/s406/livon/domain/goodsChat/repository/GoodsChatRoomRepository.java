package com.s406.livon.domain.goodsChat.repository;


import com.s406.livon.domain.goodsChat.entity.GoodsChatRoom;
import com.s406.livon.domain.user.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface GoodsChatRoomRepository extends JpaRepository<GoodsChatRoom, Long> {
//
//    // todo 수정
    @Query("""
            SELECT cr
            FROM GoodsChatRoom cr
            JOIN cr.chatParts cp
            WHERE cr.consultation.id = :consultationId
            AND cp.user.id = :user
            AND cp.isActive = true
            """)
    Optional<GoodsChatRoom> findExistingChatRoom(@Param("consultationId") Long consultationId, @Param("user") UUID user);

    Optional<GoodsChatRoom> findByConsultationId(Long consultationId);


//    @Query("""
//            SELECT cr
//            FROM GoodsChatRoom cr
//            JOIN FETCH cr.chatParts cp
//            JOIN FETCH cr.goodsPost gp
//            JOIN FETCH cp.member m
//            WHERE cr.id IN (
//                SELECT gcr.id
//                FROM GoodsChatRoom gcr
//                JOIN gcr.chatParts gcp
//                WHERE gcp.member.id = :memberId
//                AND gcp.isActive = true
//            )
//            ORDER BY cr.lastChatSentAt DESC
//            """)
//    Page<GoodsChatRoom> findChatRoomPageByMemberId(@Param("memberId") Long memberId, Pageable pageable);
//
//    @Query("""
//            SELECT cr
//            FROM GoodsChatRoom cr
//            JOIN FETCH cr.goodsPost gp
//            WHERE cr.id = :chatRoomId
//            """)
//    Optional<GoodsChatRoom> findByChatRoomId(@Param("chatRoomId") Long chatRoomId);
}
