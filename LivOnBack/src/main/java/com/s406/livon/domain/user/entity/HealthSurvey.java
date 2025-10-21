package com.s406.livon.domain.user.entity;


import com.s406.livon.domain.user.dto.request.HealthSurveyRequestDto;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class HealthSurvey {
    @Id
    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    private UUID id;

    @MapsId // User의 PK(UUID)를 공유
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column
    private int steps;

    @Column
    private int sleepTime;

    @Column
    private String disease;

    @Column
    private String sleepQuality;

    @Column
    private String medicationsInfo;

    @Column
    private String painArea;

    @Column
    private String stressLevel;

    @Column
    private String smokingStatus;

    @Column
    private int avgSleepHours;

    @Column
    private String activityLevel;

    @Column
    private String caffeineIntakeLevel;

    public void update(HealthSurveyRequestDto dto) {
        // 모든 필드를 DTO의 값으로 설정
        this.steps = dto.getSteps();
        this.sleepTime = dto.getSleepTime();
        this.disease = dto.getDisease();
        this.sleepQuality = dto.getSleepQuality();
        this.medicationsInfo = dto.getMedicationsInfo();
        this.painArea = dto.getPainArea();
        this.stressLevel = dto.getStressLevel();
        this.smokingStatus = dto.getSmokingStatus();
        this.avgSleepHours = dto.getAvgSleepHours();
        this.activityLevel = dto.getActivityLevel();
        this.caffeineIntakeLevel = dto.getCaffeineIntakeLevel();

        // 주의: @Getter만 사용했으므로, 이 엔티티 클래스 내부에서는
        // this.필드명 = 값; 형태로 직접 접근해야 합니다. (Setter 사용 불가)
    }


}
