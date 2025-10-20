package com.s406.livon.domain.user.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 프로필 이미지 변경 요청 DTO
 */
@Getter
@NoArgsConstructor
public class UpdateProfileImageDto {
    /** 프로필 이미지 URL */
    private String profileImageUrl;
}