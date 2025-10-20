package com.s406.livon.domain.user.dto.response;

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
  private String username;
  private String nickname;

  static public UserDto toDto(User user) {
    return UserDto.builder()
        .id(user.getId())
        .username(user.getEmail())
        .nickname(user.getNickname())
        .build();
  }

  public User toEntity() {
    return User.builder()
        .id(id)
        .email(username)
        .nickname(nickname)
        .build();
  }
}
