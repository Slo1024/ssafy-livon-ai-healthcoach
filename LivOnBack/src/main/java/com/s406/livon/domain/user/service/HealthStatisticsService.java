package com.s406.livon.domain.user.service;

import com.s406.livon.domain.user.entity.HealthStatistics;
import com.s406.livon.domain.user.repository.HealthStatisticsRepository;
import com.s406.livon.domain.user.repository.HealthSurveyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HealthStatisticsService {

    private final HealthStatisticsRepository healthStatisticsRepository;
    private final HealthSurveyRepository healthSurveyRepository;

    @Transactional
    public void updateHealthStatistics() {
        // 1. 평균값 계산 (JPQL 또는 QueryDSL 사용)
        Double avgSteps = healthSurveyRepository.calculateAverageSteps();
        Double avgSleepTime = healthSurveyRepository.calculateAverageSleepTime();
        Double avgHeight = healthSurveyRepository.calculateAverageHeight();
        Double avgWeight = healthSurveyRepository.calculateAverageWeight();

        // 2. 기존 통계 조회 (없으면 새로 생성)
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
    }
}