package com.s406.livon.domain.goodsChat.entity;


import com.s406.livon.domain.coach.entity.Consultation;
import com.s406.livon.domain.user.entity.User;
import com.s406.livon.domain.user.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "goods_chat_room")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GoodsChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id", nullable = false)
    private Consultation consultation;  // 예약으로 변경

    @Column(name = "last_chat_content", columnDefinition = "TEXT")
    private String lastChatContent;

    @Column(name = "last_chat_sent_at")
    private LocalDateTime lastChatSentAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "goodsChatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GoodsChatPart> chatParts = new ArrayList<>();

    public void addChatParticipant(User user, Role role) {
        GoodsChatPart chatPart = GoodsChatPart.builder()
                .goodsChatRoom(this)
                .user(user)
                .role(role)
                .build();

        chatParts.add(chatPart);
    }

    public void updateLastChat(String lastChatContent, LocalDateTime lastChatSentAt) {
        this.lastChatContent = lastChatContent;
        this.lastChatSentAt = lastChatSentAt;
    }


    public void deactivateRoom() {
        this.isActive = false;
    }

    public boolean isRoomActive() {
        return isActive;
    }
}