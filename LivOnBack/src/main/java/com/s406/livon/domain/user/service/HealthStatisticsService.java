package com.s406.livon.domain.user.service;

import com.s406.livon.domain.user.entity.HealthStatistics;
import com.s406.livon.domain.user.repository.HealthStatisticsRepository;
import com.s406.livon.domain.user.repository.HealthSurveyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HealthStatisticsService {

    private final HealthStatisticsRepository healthStatisticsRepository;
    private final HealthSurveyRepository healthSurveyRepository;

    /**
     * 건강 통계 업데이트 (기본 방식 - 4개의 쿼리)
     * @return 실행 시간 (ms)
     */
    @Transactional
    public long updateHealthStatistics() {
        StopWatch sw = new StopWatch();
        sw.start();

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

        sw.stop();
        return sw.getTotalTimeMillis();
    }

    /**
     * 건강 통계 업데이트 (최적화 버전 - 1개의 쿼리)
     * @return 실행 시간 (ms)
     */
    @Transactional
    public long updateHealthStatisticsOptimized() {
        StopWatch sw = new StopWatch();
        sw.start();

        // 1. 한 번의 쿼리로 모든 평균값 계산
        List<Object[]> result = healthSurveyRepository.calculateAllAverages();

        // ✅ 수정: List에서 첫 번째 행 가져오기
        Object[] averages = result.get(0);

        Double avgSteps = (Double) averages[0];
        Double avgSleepTime = (Double) averages[1];
        Double avgHeight = (Double) averages[2];
        Double avgWeight = (Double) averages[3];

        // 2. 기존 통계 조회 또는 생성
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

        sw.stop();
        return sw.getTotalTimeMillis();
    }
}