package com.s406.livon.domain.user.dto;

import com.s406.livon.domain.user.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
public class JwtToken {
  private String grantType;
  private String accessToken;
  private String refreshToken;
  private Long refreshTokenExpirationTime;
  private List<Role> role;

}