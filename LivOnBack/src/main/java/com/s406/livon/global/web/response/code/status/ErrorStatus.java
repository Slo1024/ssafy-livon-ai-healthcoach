package com.s406.livon.global.web.response.code.status;


import com.s406.livon.global.web.response.code.BaseErrorCode;
import com.s406.livon.global.web.response.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    // 일반적인 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON400","잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON401","인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),


    // Token 응답

    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "TOKEN4001", "토큰이 만료되었습니다"),
    TOKEN_SIGNATURE_INVALID(HttpStatus.UNAUTHORIZED, "TOKEN4002", "잘못된 토큰 입니다."),
    UNSUPPORTED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN4003", "지원되지 않는 JWT 토큰입니다."),
    INVALID_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN4004", "JWT 토큰이 잘못되었습니다."),
    REFRESH_TOKEN_NOT_VALID(HttpStatus.UNAUTHORIZED, "TOKEN4005","Refresh Token 정보가 유효하지 않습니다."),
    REFRESH_TOKEN_NOT_MATCH(HttpStatus.UNAUTHORIZED, "TOKEN4005-1","Refresh Token 정보가 일치하지 않습니다."),
    TOKEN_IS_NOT_AUTHORITY(HttpStatus.UNAUTHORIZED,"TOKEN4006","권한 정보가 없는 토큰입니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "TOKEN4007","Refresh Token이 만료되었습니다."),
    NO_AUTHENTICATION_INFORMATION(HttpStatus.UNAUTHORIZED,"TOKEN4008","인증 정보가 없는 토큰입니다."),

    // user 응답
    USER_ID_IN_USE(HttpStatus.BAD_REQUEST, "USER4000", "사용중인 이메일 입니다."),
    USER_ID_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER4004", "아이디를 잘못 입력했습니다"),
    USER_NICKNAME_IN_USE(HttpStatus.BAD_REQUEST, "USER4001", "사용중인 닉네임 입니다"),
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER4002", "해당 유저가 없습니다"),
    USER_NOT_AUTHORITY(HttpStatus.UNAUTHORIZED, "USER4006", "권한이 없습니다"),
    USER_INVALID_CREDENTIALS(HttpStatus.BAD_REQUEST, "USER4003", "로그인 정보가 일치하지 않습니다."),
    USER_NOT_MATCH(HttpStatus.UNAUTHORIZED, "USER4005", "접근 권한이 없습니다."),
    FCM_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER4010", "해당 유저의 FCM토큰이 없습니다"),
    USER_NOT_FOUND_HEALTH(HttpStatus.INTERNAL_SERVER_ERROR,"USER4007","유저 생체 데이터를 찾을수 없습니다"),
    USER_NOT_FOUND_ORGANIZATIONS(HttpStatus.INTERNAL_SERVER_ERROR,"USER4008","회사를 찾을수 없습니다"),
    // password 응답
    PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST, "PASSWORD4000", "비밀번호가 맞지 않습니다."),
    PASSWORD_SAME_AS_OLD(HttpStatus.CONFLICT, "PASSWORD4090", "기존 비밀번호와 동일합니다"),

    // mail 응답
    MAIL_SEND_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "MAIL5000", "이메일 전송에 에러가 발생했습니다."),
    MAIL_NUMBER_IS_NULL(HttpStatus.BAD_REQUEST,"MAIL4000","인증번호를 입력해주세요"),
    MAIL_NUMBER_IS_NOT_MATCH(HttpStatus.BAD_REQUEST,"MAIL4000","인증번호가 틀렸습니다"),

    // coach 정보 조회 관련
    USER_NOT_COACH(HttpStatus.BAD_REQUEST, "COACH4000", "코치가 아닙니다"),
    
    // 날짜 검증 관련
    DATE_FORM_ERROR(HttpStatus.BAD_REQUEST,"DATE4000","날짜가 형식에 맞지 않습니다"),
    DATE_PAST_DAYS(HttpStatus.BAD_REQUEST,"DATE4010","과거 날짜는 조회할 수 없습니다"),
    DATE_TOO_FAR(HttpStatus.BAD_REQUEST,"DATE4020","오늘 기준으로 30일 이후의 날짜는 조회할 수 없습니다"),
    


    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build();
    }

}
