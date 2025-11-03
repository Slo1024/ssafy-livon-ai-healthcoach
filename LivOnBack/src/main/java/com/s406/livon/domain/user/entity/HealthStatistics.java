package com.s406.livon.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "health_statistics")  // 테이블명 명시 권장
public class HealthStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "avg_steps")
    private Integer avgSteps;

    @Column(name = "avg_sleep_time")
    private Double avgSleepTime;

    @Column(name = "avg_height")
    private Double avgHeight;

    @Column(name = "avg_weight")
    private Double avgWeight;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // 마지막 계산 시간

    // 평균값 업데이트 메서드
    public void updateStatistics(Integer avgSteps, Double avgSleepTime,
                                 Double avgHeight, Double avgWeight) {
        this.avgSteps = avgSteps;
        this.avgSleepTime = avgSleepTime;
        this.avgHeight = avgHeight;
        this.avgWeight = avgWeight;
        this.updatedAt = LocalDateTime.now();
    }
}