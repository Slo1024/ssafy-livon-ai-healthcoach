package com.s406.livon.domain.goodsChat.service;


import com.s406.livon.domain.coach.entity.Consultation;
import com.s406.livon.domain.coach.repository.ConsultationRepository;
import com.s406.livon.domain.goodsChat.document.GoodsChatMessage;
import com.s406.livon.domain.goodsChat.dto.response.GoodsChatMessageResponse;
import com.s406.livon.domain.goodsChat.dto.response.GoodsChatRoomResponse;
import com.s406.livon.domain.goodsChat.entity.GoodsChatRoom;
import com.s406.livon.domain.goodsChat.entity.MessageType;
import com.s406.livon.domain.goodsChat.event.GoodsChatEvent;
import com.s406.livon.domain.goodsChat.event.GoodsChatEventPublisher;
import com.s406.livon.domain.goodsChat.repository.GoodsChatMessageRepository;
import com.s406.livon.domain.goodsChat.repository.GoodsChatRoomRepository;
import com.s406.livon.domain.user.entity.User;
import com.s406.livon.domain.user.enums.Role;
import com.s406.livon.domain.user.repository.UserRepository;
import com.s406.livon.global.error.exception.GeneralException;
import com.s406.livon.global.error.handler.UserHandler;
import com.s406.livon.global.web.response.PageResponse;
import com.s406.livon.global.web.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StopWatch;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    //    private final GoodsChatPartRepository partRepository;
    private final GoodsChatMessageRepository messageRepository;
    private final GoodsChatEventPublisher eventPublisher;
    private final ConsultationRepository consultationRepository;
    private final GoodsChatCacheManager goodsChatCacheManager;


    public GoodsChatRoomResponse getOrCreateGoodsChatRoom(UUID buyerId, Long consultationId) {
        User buyer = findUserById(buyerId);
        Consultation consultation = findConsultationId(consultationId); //todo

        User seller = findUserById(consultation.getCoach().getId());


//        validateCreateChatRoom(consultation, buyer, seller);

        // 구매자가 채팅방이 존재하면 기존 채팅방을 반환하고, 없다면 새로 생성하여 반환
        GoodsChatRoom goodsChatRoom = chatRoomRepository.findExistingChatRoom(consultationId, buyerId, Role.COACH)
                .orElseGet(() -> createChatRoom(consultation, buyer, seller));

        return GoodsChatRoomResponse.of(goodsChatRoom);
    }

//    private void validateCreateChatRoom(GoodsPost goodsPost, Member seller, Member buyer) {
//        if (goodsPost.getStatus() == Status.CLOSED) {
//            throw new CustomException(ErrorCode.GOODS_CHAT_CLOSED_POST);
//        }
//        if (seller == buyer) {
//            throw new CustomException(ErrorCode.GOODS_CHAT_SELLER_CANNOT_START);
//        }
//    }

    private GoodsChatRoom createChatRoom(Consultation consultation, User buyer, User seller) {
        GoodsChatRoom goodsChatRoom = GoodsChatRoom.builder()
                .consultation(consultation)
                .build();

        GoodsChatRoom savedChatRoom = chatRoomRepository.save(goodsChatRoom);
        savedChatRoom.addChatParticipant(buyer, Role.MEMBER);
        savedChatRoom.addChatParticipant(seller, Role.COACH);

        // 새로운 채팅방 생성 - 입장 메시지 전송
        eventPublisher.publish(GoodsChatEvent.from(goodsChatRoom.getId(), buyer, MessageType.ENTER));

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
                new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR)); //todo
    }

    //
    @Transactional(readOnly = true)
//    public List<GoodsChatMessageResponse> getChatRoomMessages(Long chatRoomId, Long memberId, LocalDateTime lastSentAt) {
    public List<GoodsChatMessage> getChatRoomMessages(Long chatRoomId, Long memberId, LocalDateTime lastSentAt) {
//        validateMemberInChatRoom(memberId, chatRoomId);
        StopWatch stopWatch = new StopWatch(); // (1) 스톱워치 생성


        List<GoodsChatMessage> chatMessages = fetchMessagesFromCacheOrDB(chatRoomId, lastSentAt, 20);

//        stopWatch.start("데이터 변환 로직"); // (2) 타이머 시작 (작업 이름 지정)
//        try {
//            List<GoodsChatMessageResponse> test = mapMessagesToResponses(chatMessages);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        stopWatch.stop(); // (3) 타이머 중지
//        System.out.println(stopWatch.prettyPrint());
//        return mapMessagesToResponses(chatMessages);
        return chatMessages;
    }

    // 채팅 내역 조회
    private List<GoodsChatMessage> fetchMessagesFromCacheOrDB(Long chatRoomId, LocalDateTime lastSentAt, int size) {

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

    // 메시지 발신자 정보 조회 및 DTO 매핑
    private List<GoodsChatMessageResponse> mapMessagesToResponses(List<GoodsChatMessage> chatMessages) {
        List<GoodsChatMessageResponse> goodsChatMessageResponses = new ArrayList<>();

        for (GoodsChatMessage chatMessage : chatMessages) {
            UUID userId = chatMessage.getUserId();
            User user = findUserById(userId);
            goodsChatMessageResponses.add(GoodsChatMessageResponse.of(chatMessage, user));
        }
        return goodsChatMessageResponses;
    }

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
