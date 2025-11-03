package com.s406.livon.domain.user.scheduler;

import com.s406.livon.domain.user.service.HealthStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HealthStatisticsScheduler {

    private final HealthStatisticsService healthStatisticsService;

    /**
     * 매일 새벽 2시에 건강 통계 평균값 계산
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void calculateHealthStatistics() {
        log.info("건강 통계 평균값 계산 시작");

        try {
            healthStatisticsService.updateHealthStatisticsOptimized();
            log.info("건강 통계 평균값 계산 완료");
        } catch (Exception e) {
            log.error("건강 통계 평균값 계산 중 오류 발생", e);
        }
    }
}