package com.s406.livon.domain.goodsChat.dto.request;

import com.s406.livon.domain.goodsChat.entity.MessageType;
import lombok.Getter;

import java.util.UUID;

@Getter
public class GoodsChatMessageRequest {

    private Long roomId;
    private String message;
    private MessageType type;
}
