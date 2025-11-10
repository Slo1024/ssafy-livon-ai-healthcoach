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
public class ChatRoomUserResponseDto {


    private final UUID userId;
    private final String userImage;
    private final String nickname;

    public static ChatRoomUserResponseDto toDto(User user){
        return ChatRoomUserResponseDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .userImage(user.getProfileImage())
                .build();
    }
    }
