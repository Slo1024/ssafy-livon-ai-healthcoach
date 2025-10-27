package com.s406.livon.domain.user.dto.response;

import com.s406.livon.domain.user.enums.Gender;
import lombok.*;

import java.util.Date;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MyInfoResponseDto {
    private String nickname;
    private String profileImage;
    private Gender gender;
    private Date birthdate;
    private HealthSurveyResponseDto healthSurvey;
}