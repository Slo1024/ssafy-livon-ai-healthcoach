package com.s406.livon.domain.user.dto.request;

import com.s406.livon.domain.user.entity.CoachInfo;
import com.s406.livon.domain.user.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class CoachInfoRequestDto {
  private String job;
  private String introduce;
  private String professional;

  public CoachInfo toEntity(User user, CoachInfoRequestDto coachInfoRequestDto) {
      return CoachInfo.builder()
              .user(user)
              .job(coachInfoRequestDto.getJob())
              .introduce(coachInfoRequestDto.getIntroduce())
              .professional(coachInfoRequestDto.getProfessional())
              .build();
  }
}