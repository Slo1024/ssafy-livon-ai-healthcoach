package com.s406.livon.domain.user.controller;


import com.s406.livon.domain.user.dto.JwtToken;
import com.s406.livon.domain.user.dto.request.*;
import com.s406.livon.domain.user.dto.response.OrganizationsResponseDto;
import com.s406.livon.domain.user.dto.response.UserDto;
import com.s406.livon.domain.user.service.UserService;
import com.s406.livon.global.security.jwt.JwtTokenProvider;
import com.s406.livon.global.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    private final JwtTokenProvider jwtTokenProvider;



    /**
     * 내 정보 보기
     */
    @GetMapping("/my-info")
    @Operation(summary = "내 정보 확인 API", description = "내 닉네임을 확인합니다.")

    public ResponseEntity<?> myInfo(@RequestHeader("Authorization") String token) {
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));
        return ResponseEntity.ok().body(ApiResponse.onSuccess(userService.myInfo(userId)));
    }


    /**
     * 이메일 중복체크
     *
     * @param email
     * @return
     */
    @PostMapping("/email-duplicate-check")
    @Operation(summary = "이메일 중복확인 API", description = "이메일을 중복을 확인합니다.")

    public ResponseEntity<?> checkEmailDuplicate(@RequestParam("email") String email) {
        String result = userService.checkEmailDuplicate(email);
        return ResponseEntity.ok().body(ApiResponse.onSuccess(result));
    }

    /**
     * 닉네임 중복체크
     *
     * @param nickname
     * @return
     */
    @PostMapping("/nickname-duplicate-check")
    @Operation(summary = "닉네임 중복확인 API", description = "닉네임 중복을 확인합니다.")
    public ResponseEntity<?> checknicknameDuplicate(@RequestParam("nickname") String nickname) {
        String result = userService.checknicknameDuplicate(nickname);
        return ResponseEntity.ok().body(ApiResponse.onSuccess(result));
    }

    /**
     * 로그인
     *
     * @param signInDto
     * @return
     */
    @PostMapping("/sign-in")
    @Operation(summary = "로그인 API", description = "로그인을 합니다.")
    public ResponseEntity<?> signIn(@RequestBody SignInDto signInDto) {
        String email = signInDto.getEmail();
        String password = signInDto.getPassword();
        JwtToken jwtToken = userService.signIn(email, password);
        return ResponseEntity.ok().body(ApiResponse.onSuccess(jwtToken));
    }

    /**
     * 회원가입
     *
     * @param signUpDto
     * @return
     */
    @PostMapping("/sign-up")
    @Operation(summary = "회원가입 API", description = "회원가입을 합니다.")
    public ResponseEntity<?> signUp(@RequestBody SignUpDto signUpDto) {
        // 회원가입 처리
        UserDto savedMemberDto = userService.signUp(signUpDto);
        return ResponseEntity.ok().body(ApiResponse.onSuccess(savedMemberDto));
    }




    /**
     * 토큰 재발급
     *
     * @param reissueDto
     * @return
     */
    @PostMapping("/token/reissue")
    @Operation(summary = "토큰 재발급 API", description = "액세스토큰,리프레쉬토큰을 재발급합니다.(RTR)")
    public ResponseEntity<?> reissue(@RequestBody ReissueDto reissueDto) {
        // 토큰 재발급 처리
        JwtToken jwtToken = userService.reissue(reissueDto);
        return ResponseEntity.ok().body(ApiResponse.onSuccess(jwtToken));
    }

    /**
     * 마이페이지 비밀번호 재설정
     * @param token 인증 토큰
     * @param dto 기존 비밀번호와 새 비밀번호
     */
    @PostMapping("/mypage-password")
    @Operation(summary = "비밀번호 재설정 API", description = "마이페이지에서 비밀번호를 재설정합니다.")
    public ResponseEntity<?> mypageResetPassword(@RequestHeader("Authorization") String token,
                                                 @RequestBody MypageResetPasswordRequestDto dto) {
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));
        userService.mypageResetPassword(userId, dto.getExsPassword(), dto.getNewPassword());
        return ResponseEntity.ok().body(ApiResponse.onSuccess(null, "비밀번호 변경 완료."));
    }

    /**
     * 비밀번호 찾기에서 비밀번호 설정
     */
    @PatchMapping("/password")
    @Operation(summary = "비밀번호 찾기 API", description = "비밀번호를 찾습니다.")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDto resetPasswordDto) {
        String result=userService.resetPassword(resetPasswordDto);
        return ResponseEntity.ok().body(ApiResponse.onSuccess(result));
    }


    @PatchMapping("/mypage-nickname")
    @Operation(summary = "닉네임 재설정 API", description = "닉네임을 재설정합니다.")
    public ResponseEntity<?> resetNickname(@RequestHeader("Authorization") String token,@RequestParam("nickname") String nickname) {
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));
        String result=userService.mypageResetNickname(userId,nickname);
        return ResponseEntity.ok().body(ApiResponse.onSuccess(result));
    }

    @DeleteMapping("/logout")
    @Operation(summary = "로그아웃 API", description = "로그아웃 합니다.")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));
        String result=userService.logout(userId);
        return ResponseEntity.ok().body(ApiResponse.onSuccess(result));
    }

    /**
     * 프로필 이미지 변경 API
     * @param token Bearer 토큰
     * @param requestDto 프로필 이미지 URL 요청 DTO
     * @return 성공 메시지
     */
    @PutMapping("/profileImage")
    @Operation(summary = "프로필 이미지 변경 API", description = "프로필 이미지를 변경합니다.")
    public ResponseEntity<?> updateProfileImage(
            @RequestHeader("Authorization") String token,
            @RequestBody UpdateProfileImageDto requestDto) {
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));
        userService.updateProfileImage(userId, requestDto.getProfileImageUrl());
        return ResponseEntity.ok().body(ApiResponse.onSuccess(null, "프로필 이미지 변경 성공"));
    }

    /**
     * 회원 탈퇴
     */
    @DeleteMapping("/delete")
    @Operation(summary = "회원 탈퇴 API", description = "회원 탈퇴를 진행합니다.")
    public ResponseEntity<?> deleteUser(@RequestHeader("Authorization") String token) {
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));
        userService.deleteUser(userId);
        return ResponseEntity.ok().body(ApiResponse.onSuccess(null, "회원탈퇴 성공"));
    }

    /**
     * 생체데이터
     * @param token
     * @param healthSurveyRequestDto
     * @return
     */

    @PostMapping("/health-survey")
    @Operation(summary = "생체데이터 API", description = "생체 데이터를 수집합니다.")
    public ResponseEntity<?> healthSurvey(@RequestHeader("Authorization") String token,@RequestBody HealthSurveyRequestDto healthSurveyRequestDto) {
        // 회원가입 처리
        UUID userId = jwtTokenProvider.getUserId(token.substring(7));
        String savedHealthSurvey = userService.healthSurvey(userId,healthSurveyRequestDto);
        return ResponseEntity.ok().body(ApiResponse.onSuccess(savedHealthSurvey));
    }


    @GetMapping("/organization")
    @Operation(summary = "전체 회사보기 API", description = "전체 회사를 조회합니다.")
    public ResponseEntity<?> healthSurvey() {
        // 회원가입 처리
        return ResponseEntity.ok().body(ApiResponse.onSuccess(userService.allOrganizations()));
    }


}

