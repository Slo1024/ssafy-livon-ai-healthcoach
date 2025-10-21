package com.s406.livon.domain.user.entity;


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



}
