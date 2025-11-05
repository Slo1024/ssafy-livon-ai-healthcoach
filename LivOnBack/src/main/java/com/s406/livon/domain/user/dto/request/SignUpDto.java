package com.s406.livon.domain.user.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.s406.livon.domain.user.entity.Organizations;
import com.s406.livon.domain.user.entity.User;
import com.s406.livon.domain.user.enums.Gender;
import com.s406.livon.domain.user.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpDto {

    private String email;
    private String password;
    private String nickname;
    private List<Role> roles = new ArrayList<>();
    private Gender gender;

    @JsonProperty(required = false)  // 선택적 필드로 명시
    private String organizations;  // null 가능 -> null 이라면 개인회원 처리

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate birthdate;

    public User toEntity(String encodedPassword, Organizations organizations, String imageUrl){
        return User.builder()
                .email(this.email)
                .password(encodedPassword)
                .nickname(this.nickname)
                .roles(roles)
                .organizations(organizations)  // null 가능
                .profileImage(imageUrl)
                .gender(this.gender)
                .birthdate(this.birthdate)
                .build();
    }
}