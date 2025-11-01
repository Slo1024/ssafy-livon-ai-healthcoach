//package com.s406.livon;
//
//import com.s406.livon.domain.goodsChat.document.GoodsChatMessage;
//import com.s406.livon.domain.goodsChat.repository.GoodsChatMessageRepository;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.util.StopWatch;
//
//@SpringBootTest
//public class GoodsChatMongoPerformanceTest { // 클래스 이름 변경 권장
//
//    // 1. Service가 아닌 Repository를 직접 주입받습니다.
//    @Autowired
//    private GoodsChatMessageRepository messageRepository;
//
//    @Test
//    void testMongoDBChatMessagePaginationPerformance() {
//        Long chatRoomId = 1L; // 테스트 채팅방 ID
//
//        // 2. 더 의미 있는 테스트를 위해 페이지 크기와 테스트할 페이지 수를 늘립니다.
//        int pageSize = 100; // 한 번에 100개씩 조회
//        int totalPagesToTest = 1; // 50 페이지 (총 5,000개 메시지) 조회
//
//        StopWatch stopWatch = new StopWatch();
//        stopWatch.start();
//
//        System.out.println("ChatRoomID: " + chatRoomId + ", PageSize: " + pageSize);
//
//        for (int page = 0; page < 10; page++) {
//            Pageable pageable = PageRequest.of(page, pageSize);
//
//            // 3. 서비스 대신, Repository의 몽고DB 쿼리를 직접 호출합니다.
//            Page<GoodsChatMessage> response =
//                    messageRepository.getChatMessages(chatRoomId, pageable);
////            System.out.println("전체 "+response.getTotalElements());
//            System.out.println("Page " + page + " size: " + response.getNumberOfElements());
//
//            // 4. 만약 데이터가 더 없으면 테스트를 중지합니다.
//            if (!response.hasNext()) {
//                System.out.println("총 " + (page + 1) + " 페이지에서 테스트 종료 (데이터 없음).");
//                break;
//            }
//        }
//
//        stopWatch.stop();
//        System.out.println(totalPagesToTest + " 페이지 조회 총 소요 시간: " + stopWatch.getTotalTimeMillis() + "ms");
//    }
//}