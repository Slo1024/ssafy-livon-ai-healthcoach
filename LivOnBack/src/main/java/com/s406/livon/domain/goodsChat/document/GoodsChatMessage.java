//package com.s406.livon.domain.goodsChat.document;
//
//import com.example.mate.domain.constant.MessageType;
//import lombok.*;
//import org.springframework.data.annotation.Id;
//import org.springframework.data.mongodb.core.index.CompoundIndex;
//import org.springframework.data.mongodb.core.index.CompoundIndexes;
//import org.springframework.data.mongodb.core.mapping.Document;
//import org.springframework.data.mongodb.core.mapping.Field;
//
//import java.time.LocalDateTime;
//
//@Getter
//@Builder
//@Document(collection = "goods_chat_message")
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@AllArgsConstructor(access = AccessLevel.PRIVATE)
//@CompoundIndexes({
//        @CompoundIndex(name = "idx_chat_room_id_sent_at", def = "{ 'chat_room_id': 1, 'sent_at': -1 }")
//})
//public class GoodsChatMessage {
//
//    @Id
//    private String id;
//
//    @Field(name = "chat_room_id")
//    private Long chatRoomId;
//
//    @Field(name = "user_id")
//    private Long memberId;
//
//    @Field(name = "content")
//    private String content;
//
//    @Field(name = "sent_at")
//    private LocalDateTime sentAt;
//
//    @Field(name = "message_type")
//    private MessageType messageType;
//
//}
