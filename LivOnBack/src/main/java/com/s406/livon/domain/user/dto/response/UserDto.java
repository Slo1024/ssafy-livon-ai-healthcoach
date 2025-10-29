package com.s406.livon.domain.user.dto.response;

import com.s406.livon.domain.user.entity.Organizations;
import com.s406.livon.domain.user.entity.User;
import lombok.*;

import java.util.UUID;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {

  private UUID id;
  private String email;
  private String nickname;
  private Organizations organizations;

  static public UserDto toDto(User user) {
    return UserDto.builder()
        .id(user.getId())
        .email(user.getEmail())
        .nickname(user.getNickname())
        .organizations(user.getOrganizations() != null
                ? user.getOrganizations()
                : null)
        .build();
  }

  public User toEntity() {
    return User.builder()
        .id(id)
        .email(email)
        .nickname(nickname)
        .build();
  }
}
