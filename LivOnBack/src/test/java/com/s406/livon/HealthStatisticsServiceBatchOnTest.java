package com.s406.livon;

import com.s406.livon.domain.user.service.HealthStatisticsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("batch-on")
class HealthStatisticsServiceBatchOnTest {

    @Autowired
    private HealthStatisticsService healthStatisticsService;

    @Test
    @DisplayName("B 시나리오: Batch Size = 1000")
    void testWithBatch() {
        System.out.println("========================================");
        System.out.println("B 시나리오: Batch Size = 1000");
        System.out.println("========================================");
        
        // Warm-up
        healthStatisticsService.updateHealthStatistics();
        
        // 실제 측정 (5회 반복)
        long totalTime = 0;
        int iterations = 5;
        
        for (int i = 1; i <= iterations; i++) {
            System.out.println("--- 실행 #" + i + " ---");
            long executionTime = healthStatisticsService.updateHealthStatistics();
            totalTime += executionTime;
        }
        
        double avgTime = totalTime / (double) iterations;
        System.out.println("\n=== B 시나리오 결과 ===");
        System.out.println("평균 실행 시간: " + String.format("%.2f", avgTime) + "ms");
        System.out.println("총 실행 시간: " + totalTime + "ms");
    }

    @Test
    @DisplayName("최적화된 버전 성능 비교")
    void testOptimizedVersion() {
        System.out.println("========================================");
        System.out.println("최적화 버전 테스트 (단일 쿼리)");
        System.out.println("========================================");
        
        // Warm-up
        healthStatisticsService.updateHealthStatisticsOptimized();
        
        // 실제 측정
        long totalTime = 0;
        int iterations = 5;
        
        for (int i = 1; i <= iterations; i++) {
            System.out.println("--- 실행 #" + i + " ---");
            long executionTime = healthStatisticsService.updateHealthStatisticsOptimized();
            totalTime += executionTime;
        }
        
        double avgTime = totalTime / (double) iterations;
        System.out.println("\n=== 최적화 버전 결과 ===");
        System.out.println("평균 실행 시간: " + String.format("%.2f", avgTime) + "ms");
        System.out.println("총 실행 시간: " + totalTime + "ms");
    }
}