package com.s406.livon.domain.goodsChat.service;


import com.s406.livon.domain.coach.entity.Consultation;
import com.s406.livon.domain.coach.repository.ConsultationRepository;
import com.s406.livon.domain.coach.repository.ParticipantRepository;
import com.s406.livon.domain.goodsChat.document.GoodsChatMessage;
import com.s406.livon.domain.goodsChat.dto.response.GoodsChatRoomResponse;
import com.s406.livon.domain.goodsChat.entity.GoodsChatPart;
import com.s406.livon.domain.goodsChat.entity.GoodsChatRoom;
import com.s406.livon.domain.goodsChat.entity.MessageType;
import com.s406.livon.domain.goodsChat.event.GoodsChatEvent;
import com.s406.livon.domain.goodsChat.event.GoodsChatEventPublisher;
import com.s406.livon.domain.goodsChat.repository.GoodsChatMessageRepository;
import com.s406.livon.domain.goodsChat.repository.GoodsChatPartRepository;
import com.s406.livon.domain.goodsChat.repository.GoodsChatRoomRepository;
import com.s406.livon.domain.user.entity.User;
import com.s406.livon.domain.user.enums.Role;
import com.s406.livon.domain.user.repository.UserRepository;
import com.s406.livon.global.error.handler.ChatHandler;
import com.s406.livon.global.error.handler.UserHandler;
import com.s406.livon.global.web.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class GoodsChatService {

    //todo
//    private final GoodsPostRepository goodsPostRepository;
    private final UserRepository userRepository;
    private final GoodsChatRoomRepository chatRoomRepository;
    private final GoodsChatPartRepository partRepository;
    private final GoodsChatMessageRepository messageRepository;
    private final GoodsChatEventPublisher eventPublisher;
    private final ConsultationRepository consultationRepository;
    private final GoodsChatCacheManager goodsChatCacheManager;
    private final ParticipantRepository participantRepository;
    private final GoodsChatPartRepository goodsChatPartRepository;
    private final com.s406.livon.domain.goodsChat.event.ChatHandler chatHandler;

    @Transactional
    public GoodsChatRoomResponse getOrCreateGoodsChatRoom(User user, Long consultationId) {
        Consultation consultation = findConsultationId(consultationId);
        //ParticipantRepositoy에 해당 방과 해당유저가 있으면 참
        boolean isParticipant = participantRepository.existsByUserIdAndConsultationId(user.getId(), consultationId);
        if (!isParticipant) {
            // 참여자가 아니면 권한 없음

            throw new ChatHandler(ErrorStatus.USER_NOT_PARTICIPANT_VALID); // (적절한 ErrorStatus로 변경)
        }

        // 기존 채팅방이 있으면 그냥참여 아니면 채팅방 만든 후 참여

        GoodsChatRoom chatRoom = chatRoomRepository.findByConsultationId(consultationId)
                .orElseGet(() -> createChatRoom(consultation, user));
        boolean userInRoom = goodsChatPartRepository.existsByUserIdAndGoodsChatRoomId(user.getId(), chatRoom.getId());
        if (!userInRoom) {
//            chatRoom.addChatParticipant(user); 안됌이거 영속성 에러
            goodsChatPartRepository.save(GoodsChatPart.builder()
                    .goodsChatRoom(chatRoom)
                    .user(user)
                    .isActive(true)
                    .build());
            eventPublisher.publish(GoodsChatEvent.from(chatRoom.getId(), user, MessageType.ENTER));
        }

        return GoodsChatRoomResponse.of(chatRoom);
    }

    @Transactional
    public GoodsChatRoom createChatRoom(Consultation consultation, User user) {
        GoodsChatRoom goodsChatRoom = GoodsChatRoom.builder()
                .consultation(consultation)
                .build();
//        goodsChatRoom.addChatParticipant(user);
        GoodsChatRoom savedChatRoom = chatRoomRepository.save(goodsChatRoom);
//        savedChatRoom.addChatParticipant(user);
        goodsChatPartRepository.save(GoodsChatPart.builder()
                        .goodsChatRoom(savedChatRoom)
                        .user(user)
                        .isActive(true)
                .build());

        // 새로운 채팅방 생성 - 입장 메시지 전송
        eventPublisher.publish(GoodsChatEvent.from(goodsChatRoom.getId(), user, MessageType.ENTER));

        return savedChatRoom;
    }

    //
    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
    }

    //
    private Consultation findConsultationId(Long goodsPostId) {
        return consultationRepository.findById(goodsPostId).orElseThrow(() ->
                new ChatHandler(ErrorStatus.CONSULTATION_NOT_FOUND));
    }

    //
    @Transactional(readOnly = true)
    public List<GoodsChatMessage> getChatRoomMessages(Long chatRoomId, UUID userId, LocalDateTime lastSentAt) {
        validateMemberInChatRoom(userId, chatRoomId);
//        StopWatch stopWatch = new StopWatch(); // (1) 스톱워치 생성
//
//        System.out.println(chatRoomId+" "+ lastSentAt+ " ");
        List<GoodsChatMessage> chatMessages = fetchMessagesFromCacheOrDB(chatRoomId, lastSentAt, 20);

//        stopWatch.start("데이터 변환 로직"); // (2) 타이머 시작 (작업 이름 지정)
//        try {
//            List<GoodsChatMessage> test = fetchMessagesFromCacheOrDB(chatRoomId, lastSentAt, 20);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        stopWatch.stop(); // (3) 타이머 중지
//        System.out.println(stopWatch.prettyPrint());
        return chatMessages;
//        return chatMessages;
    }

    private void validateMemberInChatRoom(UUID userId, Long chatRoomId) {
        if (!partRepository.existsByUserIdAndGoodsChatRoomId(userId,chatRoomId)) {
            throw new ChatHandler(ErrorStatus.USER_NOT_SELECT_VALID);
        }
    }


    // 채팅 내역 조회
    public List<GoodsChatMessage> fetchMessagesFromCacheOrDB(Long chatRoomId, LocalDateTime lastSentAt, int size) {

        // 1. redis 캐싱 데이터 조회

        List<GoodsChatMessage> chatMessages = goodsChatCacheManager.fetchMessagesFromCache(chatRoomId, lastSentAt, size);

        // 2. 데이터가 비어있는 경우, DB 에서 size 만큼 조회
        if (chatMessages.isEmpty()) {
            chatMessages = messageRepository.getChatMessages(chatRoomId, lastSentAt, size);
            // 2-1. redis 저장
            goodsChatCacheManager.storeMessagesInCache(chatRoomId, chatMessages);
            System.out.println("mongodb저장 및 조회");
        }
        // 3. 데이터가 size 보다 적은 경우
        else if (chatMessages.size() < size) {
            // 3-1. 캐싱 데이터의 마지막 보낸 시간 추출
            lastSentAt = chatMessages.get(chatMessages.size() - 1).getSentAt();

            // 3-2. 부족한 개수만큼 DB 에서 조회 후 추가
            List<GoodsChatMessage> additionalMessages = messageRepository.getChatMessages(chatRoomId, lastSentAt, size - chatMessages.size());
            chatMessages.addAll(additionalMessages);

            // 3-3. redis 저장
            goodsChatCacheManager.storeMessagesInCache(chatRoomId, additionalMessages);
            System.out.println("사이즈가 없는만큼 mongodb저장 및 조회");
        }
        return chatMessages;
    }

    public void getChatUsersInfo(Long chatRoomId, User user) {
        System.out.println(chatHandler.getConnectedUsers(3L));
    }

    // 메시지 발신자 정보 조회 및 DTO 매핑
//    private List<GoodsChatMessageResponse> mapMessagesToResponses(List<GoodsChatMessage> chatMessages) {
//        List<GoodsChatMessageResponse> goodsChatMessageResponses = new ArrayList<>();
//
//        for (GoodsChatMessage chatMessage : chatMessages) {
//            UUID userId = chatMessage.getUserId();
//            User user = findUserById(userId);
//            goodsChatMessageResponses.add(GoodsChatMessageResponse.of(chatMessage, user));
//        }
//        return goodsChatMessageResponses;
//    }

//
//    private void validateMemberParticipation(Long memberId, Long chatRoomId) {
//        if (!partRepository.existsById(new GoodsChatPartId(memberId, chatRoomId))) {
//            throw new CustomException(ErrorCode.GOODS_CHAT_NOT_FOUND_CHAT_PART);
//        }
//    }
//
//    @Transactional(readOnly = true)
//    public PageResponse<GoodsChatRoomSummaryResponse> getGoodsChatRooms(Long memberId, Pageable pageable) {
//        Member member = findMemberById(memberId);
//        Pageable validatePageable = PageResponse.validatePageable(pageable);
//
//        Page<GoodsChatRoom> chatRoomPage = chatRoomRepository.findChatRoomPageByMemberId(memberId, validatePageable);
//
//        List<GoodsChatRoomSummaryResponse> content = chatRoomPage.getContent().stream()
//                .map(chatRoom -> GoodsChatRoomSummaryResponse.of(chatRoom, getOpponentMember(chatRoom, member)))
//                .toList();
//
//        return PageResponse.from(chatRoomPage, content);
//    }
//
//    // 채팅 참여 테이블에서 상대방 회원 정보를 찾음
//    private Member getOpponentMember(GoodsChatRoom chatRoom, Member member) {
//        return chatRoom.getChatParts().stream()
//                .filter(part -> part.getMember() != member)
//                .findAny()
//                .map(GoodsChatPart::getMember)
//                .orElseThrow(() -> new CustomException(ErrorCode.GOODS_CHAT_OPPONENT_NOT_FOUND));
//    }
//
//    @Transactional(readOnly = true)
//    public GoodsChatRoomResponse getGoodsChatRoomInfo(Long memberId, Long chatRoomId) {
//        validateMemberParticipation(memberId, chatRoomId);
//
//        GoodsChatRoom goodsChatRoom = chatRoomRepository.findByChatRoomId(chatRoomId)
//                .orElseThrow(() -> new CustomException(ErrorCode.GOODS_CHAT_ROOM_NOT_FOUND));
//        return GoodsChatRoomResponse.of(goodsChatRoom);
//    }
//
//    @Transactional(readOnly = true)
//    public List<MemberSummaryResponse> getChatRoomMembers(Long memberId, Long chatRoomId) {
//        validateMemberParticipation(memberId, chatRoomId);
//        List<GoodsChatPart> goodsChatParts = partRepository.findAllWithMemberByChatRoomId(chatRoomId);
//
//        return goodsChatParts.stream()
//                .map(part -> MemberSummaryResponse.from(part.getMember()))
//                .collect(Collectors.toList());
//    }
//
//    public void deactivateGoodsChatPart(Long memberId, Long chatRoomId) {
//        Member member = findMemberById(memberId);
//        GoodsChatPart goodsChatPart = partRepository.findById(new GoodsChatPartId(memberId, chatRoomId))
//                .orElseThrow(() -> new CustomException(ErrorCode.GOODS_CHAT_NOT_FOUND_CHAT_PART));
//
//        if (!goodsChatPart.leaveAndCheckRoomStatus()) {
//            // 퇴장 메시지 전송
//            eventPublisher.publish(GoodsChatEvent.from(chatRoomId, member, MessageType.LEAVE));
//        } else {
//            // 모두 나갔다면 채팅방, 채팅 참여, 채팅 삭제
//            chatRoomRepository.deleteById(chatRoomId);
//        }
//    }
}
