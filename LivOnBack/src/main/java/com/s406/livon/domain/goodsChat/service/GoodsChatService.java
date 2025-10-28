package com.s406.livon.domain.goodsChat.service;


import com.s406.livon.domain.coach.entity.Consultation;
import com.s406.livon.domain.coach.repository.ConsultationRepository;
import com.s406.livon.domain.goodsChat.dto.response.GoodsChatMessageResponse;
import com.s406.livon.domain.goodsChat.dto.response.GoodsChatRoomResponse;
import com.s406.livon.domain.goodsChat.entity.GoodsChatMessage;
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
import com.s406.livon.global.error.exception.GeneralException;
import com.s406.livon.global.error.handler.UserHandler;
import com.s406.livon.global.web.response.PageResponse;
import com.s406.livon.global.web.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    public GoodsChatRoomResponse getOrCreateGoodsChatRoom(UUID buyerId, Long consultationId) {
        User buyer = findUserById(buyerId);
        Consultation consultation = findConsultationId(consultationId); //todo

        User seller = findUserById(consultation.getUserId());


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
    public PageResponse<GoodsChatMessageResponse> getMessagesForChatRoom(Long chatRoomId, Long memberId, Pageable pageable) {
//        validateMemberParticipation(memberId, chatRoomId);
        Pageable validatePageable = PageResponse.validatePageable(pageable);

        Page<GoodsChatMessage> chatMessagePage = messageRepository.findByChatRoomId(chatRoomId, validatePageable);
        List<GoodsChatMessageResponse> content = chatMessagePage.getContent().stream()
                .map(GoodsChatMessageResponse::of)
                .toList();

        return PageResponse.from(chatMessagePage, content);
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
