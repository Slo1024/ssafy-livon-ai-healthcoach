package com.s406.livon.domain.user.dto.request;


import com.s406.livon.domain.user.entity.HealthSurvey;
import com.s406.livon.domain.user.entity.User;
import jakarta.persistence.Column;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthSurveyRequestDto {
    private int steps;
    private int sleepTime;
    private String disease;
    private String sleepQuality;
    private String medicationsInfo;
    private String painArea;
    private String stressLevel;
    private String smokingStatus;
    private int avgSleepHours;
    private String activityLevel;
    private String caffeineIntakeLevel;

    public HealthSurvey toEntity(User user){
        return HealthSurvey.builder()
                .user(user)
                .steps(this.steps)
                .sleepTime(this.sleepTime)
                .disease(this.disease)
                .sleepQuality(this.sleepQuality)
                .medicationsInfo(this.medicationsInfo)
                .painArea(this.painArea)
                .stressLevel(this.stressLevel)
                .smokingStatus(this.smokingStatus)
                .avgSleepHours(this.avgSleepHours)
                .activityLevel(this.activityLevel)
                .caffeineIntakeLevel(this.caffeineIntakeLevel)
                .build();
    }
}
