package com.s406.livon.domain.user.dto.request;

import com.s406.livon.domain.user.entity.User;
import com.s406.livon.domain.user.enums.Gender;
import com.s406.livon.domain.user.enums.Role;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
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
    private double weight;
    private double height;
    private Gender gender;
    private int age;


    public User toEntity(String encodedPassword,List<Role> role){
        return User.builder()
                .email(this.email)
                .password(encodedPassword)
                .nickname(this.nickname)
                .profileImage(this.profileImage)
                .weight(this.weight)
                .roles(role)
                .height(this.height)
                .gender(this.gender)
                .build();
    }
}