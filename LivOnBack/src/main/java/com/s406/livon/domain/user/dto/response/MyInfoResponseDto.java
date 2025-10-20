package com.s406.livon.domain.user.dto.response;

import lombok.*;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MyInfoResponseDto {
    private String nickname;
    private String university;
    private String major;
    private String profileImage;
}
