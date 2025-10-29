package com.s406.livon.global.web.response.code.status;


import com.s406.livon.global.web.response.code.BaseCode;
import com.s406.livon.global.web.response.code.ReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessStatus implements BaseCode {

    // 일반적인 응답
    SUCCESS(HttpStatus.OK, "COMMON200", "성공입니다."),

    // 조회 성공
    SELECT_SUCCESS(HttpStatus.OK, "COMMON200", "조회 성공"),
    // 생성 성공
    INSERT_SUCCESS(HttpStatus.CREATED, "COMMON201", "생성 성공"),
    // 업데이트 성공
    UPDATE_SUCCESS(HttpStatus.OK, "COMMON200", "업데이트 성공"),
    // 삭제 성공
    DELETE_SUCCESS(HttpStatus.OK, "COMMON200", "삭제 성공"),

    // 데이터가 없을 때 응답
    NO_CONTENT(HttpStatus.NO_CONTENT, "COMMON204", "데이터가 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;


    @Override
    public ReasonDTO getReason() {
        return ReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(true)
                .build();
    }

    @Override
    public ReasonDTO getReasonHttpStatus() {
        return ReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(true)
                .httpStatus(httpStatus)
                .build();
    }
}
