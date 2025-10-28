package com.s406.livon.domain.goodsChat.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodsChatPartId implements Serializable {

    private UUID user;
    private Long goodsChatRoom;
}
