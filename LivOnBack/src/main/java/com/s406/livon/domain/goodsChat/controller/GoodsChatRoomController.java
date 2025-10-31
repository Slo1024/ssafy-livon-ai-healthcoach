package com.s406.livon.domain.goodsChat.controller;


import com.s406.livon.domain.goodsChat.document.GoodsChatMessage;
import com.s406.livon.domain.goodsChat.dto.response.GoodsChatMessageResponse;
import com.s406.livon.domain.goodsChat.dto.response.GoodsChatRoomResponse;
import com.s406.livon.domain.goodsChat.service.GoodsChatService;
import com.s406.livon.global.web.response.ApiResponse;
import com.s406.livon.global.web.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/goods/chat")
public class GoodsChatRoomController {

    private final GoodsChatService goodsChatService;

    /*
    굿즈거래 상세 페이지 - 채팅방 입장
    TODO: @RequestParam Long memberId -> @AuthenticationPrincipal 로 변경
    "/api/goods/chat" 로 변경 예정
    */
    @PostMapping
    public ResponseEntity<ApiResponse<GoodsChatRoomResponse>> createGoodsChatRoom(@RequestParam UUID buyerId,
                                                                                  @RequestParam Long consultationId) {
        GoodsChatRoomResponse response = goodsChatService.getOrCreateGoodsChatRoom(buyerId, consultationId);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    /*
    굿즈거래 채팅방 페이지 - 채팅 내역 조회
    TODO: @RequestParam Long memberId -> @AuthenticationPrincipal 로 변경
    */
    @GetMapping("/{chatRoomId}/message")
    public ResponseEntity<ApiResponse<List<GoodsChatMessage>>> getGoodsChatRoomMessages(
            @PathVariable Long chatRoomId,
            @RequestParam Long userId,
            @RequestParam(required = false) LocalDateTime lastSentAt
    ) {
        List<GoodsChatMessage> response = goodsChatService.getChatRoomMessages(chatRoomId, userId, lastSentAt);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    /*
//    굿즈거래 채팅방 리스트 페이지 - 내가 참여한 채팅방 리스트 조회
//    TODO: @RequestParam Long memberId -> @AuthenticationPrincipal 로 변경
//    */
//    @GetMapping
//    public ResponseEntity<ApiResponse<PageResponse<GoodsChatRoomSummaryResponse>>> getGoodsChatRooms(@RequestParam Long memberId,
//                                                                                                     @PageableDefault Pageable pageable) {
//        PageResponse<GoodsChatRoomSummaryResponse> response = goodsChatService.getGoodsChatRooms(memberId, pageable);
//        return ResponseEntity.ok(ApiResponse.success(response));
//    }
//
//    // 채팅방 나가기
//    @DeleteMapping("/{chatRoomId}")
//    public ResponseEntity<Void> leaveGoodsChatRoom(@RequestParam Long memberId, @PathVariable Long chatRoomId) {
//        goodsChatService.deactivateGoodsChatPart(memberId, chatRoomId);
//
//        return ResponseEntity.noContent().build();
//    }
//
//    /*
//    굿즈거래 채팅방 리스트 페이지 - 채팅방 단건 조회
//    TODO: @RequestParam Long memberId -> @AuthenticationPrincipal 로 변경
//    */
//    @GetMapping("/{chatRoomId}")
//    public ResponseEntity<ApiResponse<GoodsChatRoomResponse>> getGoodsChatRoomInfo(@RequestParam Long memberId,
//                                                                                   @PathVariable Long chatRoomId) {
//        GoodsChatRoomResponse response = goodsChatService.getGoodsChatRoomInfo(memberId, chatRoomId);
//        return ResponseEntity.ok(ApiResponse.success(response));
//    }
//
//    // 채팅방 하단 토글 - 현재 채팅에 참여한 사용자 프로필 조회
//    @GetMapping("/{chatRoomId}/members")
//    public ResponseEntity<ApiResponse<List<MemberSummaryResponse>>> getGoodsChatRoomMembers(@RequestParam Long memberId,
//                                                                                            @PathVariable Long chatRoomId) {
//        List<MemberSummaryResponse> responses = goodsChatService.getChatRoomMembers(memberId, chatRoomId);
//        return ResponseEntity.ok(ApiResponse.success(responses));
//    }
}
