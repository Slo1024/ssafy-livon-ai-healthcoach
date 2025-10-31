package com.s406.livon.domain.goodsChat.service;


import com.s406.livon.domain.goodsChat.document.GoodsChatMessage;
import com.s406.livon.domain.goodsChat.dto.request.GoodsChatMessageRequest;
import com.s406.livon.domain.goodsChat.dto.response.GoodsChatMessageResponse;
import com.s406.livon.domain.goodsChat.entity.*;
import com.s406.livon.domain.goodsChat.event.GoodsChatEvent;
import com.s406.livon.domain.goodsChat.repository.GoodsChatMessageRepository;
import com.s406.livon.domain.goodsChat.repository.GoodsChatRoomRepository;
import com.s406.livon.domain.user.entity.User;
import com.s406.livon.domain.user.repository.UserRepository;
import com.s406.livon.global.error.exception.GeneralException;
import com.s406.livon.global.error.handler.UserHandler;
import com.s406.livon.global.web.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class GoodsChatMessageService {

    private final UserRepository userRepository;
    private final GoodsChatRoomRepository chatRoomRepository;
    private final GoodsChatMessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final TransactionTemplate mongoTransactionTemplate;
    private final GoodsChatCacheManager goodsChatCacheManager;

    private static final String MEMBER_ENTER_MESSAGE = "님이 채팅방에 입장하셨습니다.";
    private static final String MEMBER_LEAVE_MESSAGE = "님이 채팅방을 떠나셨습니다.";


    public void sendMessage(GoodsChatMessageRequest message) {
        User sender = findMemberById(message.getSenderId());
        GoodsChatRoom chatRoom = findByChatRoomById(message.getRoomId());
        saveAndSendMessage(chatRoom, sender, message.getMessage(), message.getType());
    }

    private void saveAndSendMessage(GoodsChatRoom chatRoom, User user, String message, MessageType type) {
        // 채팅 도큐먼트 생성
        GoodsChatMessage chatMessage = createChatMessage(chatRoom.getId(), user.getId(), message, type);

        // 최신 채팅 내역 업데이트
        chatRoom.updateLastChat(message, chatMessage.getSentAt());

        // MongoDB 트랜잭션
        try {
            mongoTransactionTemplate.executeWithoutResult(status -> {
                // 채팅 메시지 저장
                GoodsChatMessage savedMessage = messageRepository.save(chatMessage);

                // redis 캐시 저장
                goodsChatCacheManager.storeMessageInCache(savedMessage.getChatRoomId(), savedMessage);

                // 메시지 전송
                sendToSubscribers(savedMessage.getChatRoomId(), GoodsChatMessageResponse.of(savedMessage, user));
            });
        } catch (Exception e) {
            // redis 캐시 무효화
            goodsChatCacheManager.evictMessagesFromCache(chatRoom.getId());

            // JPA 트랜잭션으로 예외 전파
            throw e;
        }
    }


    // 입장 및 퇴장 메시지 전송
    public void sendChatEventMessage(GoodsChatEvent event) {
        User user = event.user();
        Long chatRoomId = event.chatRoomId();
        Long roomId = event.chatRoomId();

        GoodsChatRoom chatRoom = findByChatRoomById(roomId);

        // 메시지 생성
        String message = user.getNickname();
        switch (event.type()) {
            case ENTER -> message += MEMBER_ENTER_MESSAGE;
            case LEAVE -> message += MEMBER_LEAVE_MESSAGE;
        }

        // Message DB에 저장
        GoodsChatMessage chatMessage = createChatMessage(chatRoomId, user.getId(), message, event.type());

        GoodsChatMessage savedMessage = messageRepository.save(chatMessage);
        chatRoom.updateLastChat(message, chatMessage.getSentAt());


        // 이벤트 메시지 전송
        sendToSubscribers(chatRoomId, GoodsChatMessageResponse.of(savedMessage, user));
    }

    private GoodsChatMessage createChatMessage(Long chatRoomId, UUID userId, String message, MessageType type) {
        return GoodsChatMessage.builder()
                .chatRoomId(chatRoomId)
                .userId(userId)
                .sentAt(LocalDateTime.now())
                .content(message)
                .messageType(type)
                .build();
    }

    private User findMemberById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
    }

    private GoodsChatRoom findByChatRoomById(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND)); //todo 에러반환 수정
    }


    private void sendToSubscribers(Long roomId, GoodsChatMessageResponse message) {
        messagingTemplate.convertAndSend("/sub/chat/goods/" + roomId, message);
    }
}
