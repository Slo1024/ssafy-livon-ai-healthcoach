//package com.s406.livon.domain.goodsChat.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "goods_chat_message")
//@Getter
//@Builder
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@AllArgsConstructor(access = AccessLevel.PRIVATE)
//public class GoodsChatMessage {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumns({
//            @JoinColumn(name = "user_id", referencedColumnName = "user_id"),
//            @JoinColumn(name = "chat_room_id", referencedColumnName = "chat_room_id")
//    })
//    private GoodsChatPart goodsChatPart;
//
//    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
//    private String content;
//
//    @Column(name = "sent_at", nullable = false)
//    private LocalDateTime sentAt;
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "message_type", nullable = false)
//    private MessageType messageType;
//
//    @PrePersist
//    public void prePersist() {
//        this.sentAt = LocalDateTime.now();
//    }
//}
