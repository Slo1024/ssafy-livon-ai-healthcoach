package com.s406.livon.domain.goodsChat.dto.response;


import com.s406.livon.domain.coach.entity.Consultation;
import com.s406.livon.domain.goodsChat.entity.GoodsChatRoom;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class GoodsChatRoomResponse {

    private final Long chatRoomId;
    private final Long consultationId;
    private final String chatRoomStatus;

    public static GoodsChatRoomResponse of(GoodsChatRoom chatRoom) {
        Consultation consultation = chatRoom.getConsultation();
//        String mainImageUrl = consultation.getMainImageUrl();

        return GoodsChatRoomResponse.builder()
                .chatRoomId(chatRoom.getId())
                .consultationId(consultation.getId())

                .chatRoomStatus(chatRoom.getIsActive().toString())
                .build();
    }

//
}
