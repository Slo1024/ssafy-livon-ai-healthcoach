package com.s406.livon.domain.auth.controller;


import com.s406.livon.domain.auth.dto.request.EmailCheckRequestDto;
import com.s406.livon.domain.auth.dto.request.EmailRequestDto;
import com.s406.livon.domain.auth.service.MailSendService;
import com.s406.livon.global.web.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/mail")

public class MailController {
    private final MailSendService mailService;


    @PostMapping("/send")
    @Operation(summary = "회원가입 인증메일 보내기 API", description = "회원가입 이메일로 인증메일을 보냅니다")
    public ResponseEntity<?> mailSend(@RequestBody @Valid EmailRequestDto emailDto) {
         mailService.joinEmail(emailDto.getEmail(),"회원가입");

        return ResponseEntity.ok().body(ApiResponse.onSuccess("메일이 전송되었습니다"));
    }


    @PostMapping("/send-password")
    @Operation(summary = "비밀번호 찾기 인증메일 보내기 API", description = "비밀번호 찾기 이메일로 인증메일을 보냅니다")
    public ResponseEntity<?> mailSendForPassword(@RequestBody @Valid EmailRequestDto emailDto) {
        String result=mailService.mailSendForPassword(emailDto.getEmail());

        return ResponseEntity.ok().body(ApiResponse.onSuccess(result));
    }

    @PostMapping("/auth-check")
    @Operation(summary = "인증번호 확인 API", description = "인증번호가 맞는지 확인합니다.")
    public ResponseEntity<?> AuthCheck(@RequestBody @Valid EmailCheckRequestDto emailCheckDto){
        String Checked=mailService.CheckAuthNum(emailCheckDto.getEmail(),emailCheckDto.getAuthNum());
        return ResponseEntity.ok().body(ApiResponse.onSuccess(Checked));

    }
}
    