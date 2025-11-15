package com.s406.livon.domain.goodsChat.controller;


import com.s406.livon.domain.goodsChat.dto.response.GoodsChatMessageResponse;
import com.s406.livon.domain.goodsChat.dto.response.GoodsChatRoomResponse;
import com.s406.livon.domain.goodsChat.service.GoodsChatService;
import com.s406.livon.domain.user.entity.User;
import com.s406.livon.global.web.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/goods/chat")
public class GoodsChatRoomController {

    private final GoodsChatService goodsChatService;

    /**
     * 채팅방 참여
     * @param user
     * @param consultationId
     * @return
     */
    @PostMapping
    public ResponseEntity<ApiResponse<GoodsChatRoomResponse>> createGoodsChatRoom(  @AuthenticationPrincipal User user,
                                                                                  @RequestParam Long consultationId) {
        GoodsChatRoomResponse response = goodsChatService.getOrCreateGoodsChatRoom(user, consultationId);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }


    /**
     * 채팅방 채팅내용 보기
     * @param chatRoomId
     * @param user
     * @param lastSentAt
     * @return
     */
    @GetMapping("/{chatRoomId}/message")
    public ResponseEntity<ApiResponse<List<GoodsChatMessageResponse>>> getGoodsChatRoomMessages(
            @PathVariable Long chatRoomId,
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) LocalDateTime lastSentAt
    ) {
        List<GoodsChatMessageResponse> response = goodsChatService.getChatRoomMessages(chatRoomId, user.getId(), lastSentAt);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    // Todo 채팅방 안 유저 정보

    @GetMapping("/{chatRoomId}/users/connection")
    public ResponseEntity<ApiResponse<?>> getChatUsersConnection(
            @PathVariable Long chatRoomId,
            @AuthenticationPrincipal User user
    ) {

        return ResponseEntity.ok(ApiResponse.onSuccess(goodsChatService.getChatUsersConnection(chatRoomId,user)));
    }

    @GetMapping("/{chatRoomId}/users")
    public ResponseEntity<ApiResponse<?>> getChatUsers(
            @PathVariable Long chatRoomId,
            @AuthenticationPrincipal User user
    ) {

        return ResponseEntity.ok(ApiResponse.onSuccess(goodsChatService.getChatUsersInfo(chatRoomId,user)));
    }

}
