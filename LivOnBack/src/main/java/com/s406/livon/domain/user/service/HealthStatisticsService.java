package com.s406.livon.domain.user.service;

import com.s406.livon.domain.user.entity.HealthStatistics;
import com.s406.livon.domain.user.repository.HealthStatisticsRepository;
import com.s406.livon.domain.user.repository.HealthSurveyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HealthStatisticsService {

    private final HealthStatisticsRepository healthStatisticsRepository;
    private final HealthSurveyRepository healthSurveyRepository;

    @Transactional
    public void updateHealthStatistics() {
        // 1. 한 번의 쿼리로 모든 평균값 계산 (DB 통신 1회)
        Object[] averages = healthSurveyRepository.calculateAllAverages();

        Double avgSteps = (Double) averages[0];
        Double avgSleepTime = (Double) averages[1];
        Double avgHeight = (Double) averages[2];
        Double avgWeight = (Double) averages[3];

        // 2. 기존 통계 조회 또는 생성 + 저장 (DB 통신 1회)
        HealthStatistics statistics = healthStatisticsRepository.findById(1L)
                .orElse(HealthStatistics.builder()
                        .id(1L)
                        .build());

        // 3. 평균값 업데이트
        statistics.updateStatistics(
                avgSteps != null ? avgSteps.intValue() : null,
                avgSleepTime,
                avgHeight,
                avgWeight
        );

        // 4. 저장
        healthStatisticsRepository.save(statistics);

        log.info("건강 통계 업데이트 완료 - Steps: {}, Sleep: {}, Height: {}, Weight: {}",
                avgSteps, avgSleepTime, avgHeight, avgWeight);
    }
}