package com.s406.livon;

import com.s406.livon.domain.user.service.HealthStatisticsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("batch-off")
class HealthStatisticsServiceBatchOffTest {

    @Autowired
    private HealthStatisticsService healthStatisticsService;

    @Test
    @DisplayName("A 시나리오: Batch Size = 0 (기본 방식)")
    void testWithoutBatch() {
        System.out.println("========================================");
        System.out.println("A 시나리오: Batch Size = 0");
        System.out.println("========================================");
        
        // Warm-up (JVM 최적화를 위한 준비 실행)
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
        System.out.println("\n=== A 시나리오 결과 ===");
        System.out.println("평균 실행 시간: " + String.format("%.2f", avgTime) + "ms");
        System.out.println("총 실행 시간: " + totalTime + "ms");
    }
}