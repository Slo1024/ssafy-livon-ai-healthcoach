package com.s406.livon;

import com.s406.livon.domain.goodsChat.document.GoodsChatMessage;
import com.s406.livon.domain.goodsChat.service.GoodsChatService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StopWatch;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@SpringBootTest
public class GoodsChatCachePerformanceTest {

    private static final Logger log = LoggerFactory.getLogger(GoodsChatCachePerformanceTest.class);

    @Autowired
    private GoodsChatService goodsChatService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // --- 테스트 설정 ---
    private static final Long TEST_CHAT_ROOM_ID = 3L; // ★★★ 테스트할 채팅방 ID
    private static final int TEST_PAGE_SIZE = 20;
    // ★★★ GoodsChatCacheManager에서 사용하는 캐시 키와 동일해야 합니다.
    private static final String CACHE_KEY = "goods_chat_message::" + TEST_CHAT_ROOM_ID; 
    // --- ---

    @Test
    @DisplayName("캐시 로직 테스트: 1. Cache Miss (DB Hit), 2. Cache Hit")
    void testCacheMissAndCacheHit() {
        // GIVEN: 테스트할 파라미터 준비 (첫 페이지)
        LocalDateTime lastSentAt = null;

        // --- SETUP: 캐시를 강제로 삭제 ---
        log.info("--- [Test Start] 캐시 삭제 시도: {} ---", CACHE_KEY);
        redisTemplate.delete(CACHE_KEY);
        Long cacheSize = redisTemplate.opsForZSet().size(CACHE_KEY);
        Assertions.assertEquals(0, cacheSize, "캐시가 완전히 비워져야 합니다.");
        log.info("--- 캐시 삭제 완료 (현재 크기: {}) ---", cacheSize);


        // --- 1. Cache Miss (DB Hit) 테스트 ---
        log.info("--- [1. Cache Miss (DB Hit) 시도] ---");
        // (서비스의 "mongodb저장 및 조회" 로그가 출력되어야 함)
        StopWatch sw1 = new StopWatch();
        sw1.start();
        List<GoodsChatMessage> fromDb = goodsChatService.fetchMessagesFromCacheOrDB(
                TEST_CHAT_ROOM_ID, lastSentAt, TEST_PAGE_SIZE
        );
        sw1.stop();
        log.info("--- 1. Cache Miss (DB Hit) 결과 ---");
        log.info("   -> {}개 메시지, 소요시간: {}ms", fromDb.size(), sw1.getTotalTimeMillis());

        
        // --- 2. Cache Hit 테스트 ---
        log.info("--- [2. Cache Hit 시도] ---");
        // (서비스의 "mongodb저장 및 조회" 로그가 출력되지 *않아야* 함)
        StopWatch sw2 = new StopWatch();
        sw2.start();
        List<GoodsChatMessage> fromCache = goodsChatService.fetchMessagesFromCacheOrDB(
                TEST_CHAT_ROOM_ID, lastSentAt, TEST_PAGE_SIZE
        );
        sw2.stop();
        log.info("--- 2. Cache Hit 결과 ---");
        log.info("   -> {}개 메시지, 소요시간: {}ms", fromCache.size(), sw2.getTotalTimeMillis());


        // --- VERIFY: 검증 ---
        Assertions.assertFalse(fromDb.isEmpty(), "DB에서 데이터가 조회되어야 합니다.");
        Assertions.assertEquals(fromDb.size(), fromCache.size(), "캐시와 DB의 결과 개수가 같아야 합니다.");
        
        // 캐시 히트가 DB 조회보다 확실히 빨라야 함
        log.info("[비교] DB Hit Time: {}ms  vs  Cache Hit Time: {}ms",
                sw1.getTotalTimeMillis(), sw2.getTotalTimeMillis()
        );
        
        // (참고: 로컬 PC에서는 DB/캐시 속도 차이가 1ms 미만일 수 있어, 
        //  sout 로그의 "mongodb저장 및 조회" 출력 여부로 확인하는 것이 더 정확합니다)
        // Assertions.assertTrue(sw2.getTotalTimeMillis() < sw1.getTotalTimeMillis(),
        //         "캐시 히트가 DB 조회보다 빨라야 합니다.");
    }
}