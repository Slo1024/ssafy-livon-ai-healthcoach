package com.s406.livon.domain.goodsChat.service;


import com.s406.livon.domain.goodsChat.document.GoodsChatMessage;
import com.s406.livon.domain.goodsChat.dto.request.GoodsChatMessageRequest;
import com.s406.livon.domain.goodsChat.dto.response.GoodsChatMessageResponse;
import com.s406.livon.domain.goodsChat.entity.*;
import com.s406.livon.domain.goodsChat.event.GoodsChatEvent;
import com.s406.livon.domain.goodsChat.repository.GoodsChatMessageRepository;
import com.s406.livon.domain.goodsChat.repository.GoodsChatPartRepository;
import com.s406.livon.domain.goodsChat.repository.GoodsChatRoomRepository;
import com.s406.livon.domain.user.entity.User;
import com.s406.livon.domain.user.enums.Role;
import com.s406.livon.domain.user.repository.UserRepository;
import com.s406.livon.global.error.exception.GeneralException;
import com.s406.livon.global.error.handler.ChatHandler;
import com.s406.livon.global.error.handler.UserHandler;
import com.s406.livon.global.web.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
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
    private final GoodsChatPartRepository goodsChatPartRepository;
    private static final String MEMBER_ENTER_MESSAGE = "님이 채팅방에 입장하셨습니다.";
    private static final String MEMBER_LEAVE_MESSAGE = "님이 채팅방을 떠나셨습니다.";


    public void sendMessage(GoodsChatMessageRequest message,User sender) {
        GoodsChatRoom chatRoom = findByChatRoomById(message.getRoomId());

        // 채팅방에 참여자가 아닐경우
        if(!goodsChatPartRepository.existsByUserIdAndGoodsChatRoomId(sender.getId(), chatRoom.getId())){
            throw new ChatHandler(ErrorStatus.USER_NOT_SEND_VALID);
        };

        saveAndSendMessage(chatRoom, sender, message.getMessage(), message.getType());
    }

    private void saveAndSendMessage(GoodsChatRoom chatRoom, User user, String message, MessageType type) {
        // 채팅 도큐먼트 생성
        GoodsChatMessage chatMessage = createChatMessage(chatRoom.getId(), user.getId(), message, user.getRoles().get(0) ,type);
        // 최근메세지 불필요
//        chatRoom.updateLastChat(message, chatMessage.getSentAt());

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
        Long roomId = event.chatRoomId();
        GoodsChatRoom chatRoom = findByChatRoomById(roomId);

        // 메시지 생성
        String message = user.getNickname();
        switch (event.type()) {
            case ENTER -> message += MEMBER_ENTER_MESSAGE;
            case LEAVE -> message += MEMBER_LEAVE_MESSAGE;
        }

        // Message DB에 저장
        saveAndSendMessage(chatRoom, user, message, event.type());

    }

    private GoodsChatMessage createChatMessage(Long chatRoomId, UUID userId, String message, Role role , MessageType type) {

        return GoodsChatMessage.builder()
                .chatRoomId(chatRoomId)
                .userId(userId)
                .sentAt(LocalDateTime.now())
                .content(message)
                .messageType(type)
                .role(role) // <-- String List로 저장
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
