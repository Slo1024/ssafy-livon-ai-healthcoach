package com.s406.livon;

import com.s406.livon.domain.goodsChat.document.GoodsChatMessage;
import com.s406.livon.domain.goodsChat.entity.MessageType;
import com.s406.livon.domain.goodsChat.repository.GoodsChatMessageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootTest
public class GoodsChatDataGeneratorTest { // 테스트 클래스 새로 생성 또는 기존에 추가

    @Autowired
    private GoodsChatMessageRepository messageRepository;

    @Test
    void generateDummyData() {
        System.out.println("MongoDB 데이터 10만건 생성 시작...");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // 1,000개씩 묶어서 100번 실행 (총 10만건)
        final int BATCH_SIZE = 1000;
        final int TOTAL_DOCS = 100000;

        // 테스트용 ID (이전에 사용하신 ID 예시)
        Long chatRoomId1 = 1L;
        Long chatRoomId2 = 2L; // 2번 방 (성능 테스트용)
        UUID userId1 = UUID.fromString("b525bb98-6090-4faf-9e6e-8ec895c9fbd9");
//        UUID userId2 = UUID.fromString("a1b2c3d4-e5f6-1234-a1b2-c3d4e5f67890");

        List<GoodsChatMessage> batchList = new ArrayList<>(BATCH_SIZE);

        for (int i = 0; i < TOTAL_DOCS; i++) {
            
            // 테스트용 데이터 생성 (2번 방에 5만건, 1번 방에 5만건)
            GoodsChatMessage message = GoodsChatMessage.builder()
                    .chatRoomId( chatRoomId1)
                    .userId(userId1) // 유저 2명 번갈아 가며
                    .content("성능 테스트 메시지 " + i)
                    .sentAt(LocalDateTime.now().minusSeconds(TOTAL_DOCS - i)) // 과거부터 현재까지 순차적
                    .messageType(MessageType.TALK)
                    .build();
            
            batchList.add(message);

            // 1. 배치가 꽉 차면 DB에 일괄 삽입 (saveAll)
            if (batchList.size() == BATCH_SIZE) {
                messageRepository.saveAll(batchList);
                batchList.clear(); // 2. 리스트를 비워 메모리 절약
                System.out.println(" - " + (i + 1) + "건 삽입 완료");
            }
        }
        
        // 3. 1000개 미만의 나머지 데이터가 있다면 삽입
        if (!batchList.isEmpty()) {
            messageRepository.saveAll(batchList);
        }

        stopWatch.stop();
        System.out.println("총 " + TOTAL_DOCS + "건 생성 완료. 총 소요 시간: " + stopWatch.getTotalTimeMillis() + "ms");
    }
}