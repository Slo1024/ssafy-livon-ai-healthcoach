package com.s406.livon.domain.goodsChat.entity;


import com.s406.livon.domain.user.entity.User;
import com.s406.livon.domain.user.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@IdClass(GoodsChatPartId.class)
@Table(name = "goods_chat_part")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GoodsChatPart {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private GoodsChatRoom goodsChatRoom;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "goodsChatPart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<GoodsChatMessage> goodsChatMessages = new ArrayList<>();

    public boolean leaveAndCheckRoomStatus() {
        if (!goodsChatRoom.isRoomActive()) {
            return true;
        }
        goodsChatRoom.deactivateRoom();
        this.isActive = false;
        return false;
    }
}
