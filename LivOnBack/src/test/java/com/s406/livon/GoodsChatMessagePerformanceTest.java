//package com.s406.livon;
//
//import com.s406.livon.domain.goodsChat.dto.response.GoodsChatMessageResponse;
//import com.s406.livon.domain.goodsChat.service.GoodsChatService;
//import com.s406.livon.global.web.response.PageResponse;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.util.StopWatch;
//
//@SpringBootTest
//public class GoodsChatMessagePerformanceTest {
//
//    @Autowired
//    private GoodsChatService goodsChatService;
//
//    @Test
//    void testChatMessagePaginationPerformance() {
//        Long chatRoomId = 2L; // 테스트 채팅방 ID
//        Long memberId = 1L; // 테스트 사용자 ID
//        int pageSize = 10;
//
//        // 1페이지부터 10페이지까지 테스트
//        StopWatch stopWatch = new StopWatch();
//        stopWatch.start();
//
//        for (int page = 0; page < 10; page++) {
//            Pageable pageable = PageRequest.of(page, pageSize);
//            PageResponse<GoodsChatMessageResponse> response =
//                    goodsChatService.getMessagesForChatRoom(chatRoomId, memberId, pageable);
//
//            System.out.println("Page " + page + " size: ");
//        }
//
//        stopWatch.stop();
//        System.out.println("총 소요 시간: " + stopWatch.getTotalTimeMillis() + "ms");
//    }
//}
