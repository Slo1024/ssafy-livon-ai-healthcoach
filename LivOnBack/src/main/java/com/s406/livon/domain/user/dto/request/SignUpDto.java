package com.s406.livon.domain.user.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.s406.livon.domain.user.entity.Organizations;
import com.s406.livon.domain.user.entity.User;
import com.s406.livon.domain.user.enums.Gender;
import com.s406.livon.domain.user.enums.Role;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpDto {

    private String email; // 이메일
    private String password; // 비밀번호
    private String nickname; //닉네임
    private String profileImage;
    private List<Role> roles = new ArrayList<>();
    private Gender gender;
    private String organizations;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private Date birthdate;
    // 로그인 시 받는 정보 중 몸무게/키 삭제
    // 기존에 int age로 받던 정보를 Date birthdate로 변경

    public User toEntity(String encodedPassword,Organizations organizations){
        return User.builder()
                .email(this.email)
                .password(encodedPassword)
                .nickname(this.nickname)
                .profileImage(this.profileImage)
                .roles(this.roles)
                .organizations(organizations)
                .gender(this.gender)
                .birthdate(this.birthdate)
                .build();
    }
}