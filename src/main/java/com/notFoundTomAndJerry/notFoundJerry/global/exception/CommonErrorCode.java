package com.notFoundTomAndJerry.notFoundJerry.global.exception;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum CommonErrorCode implements ApiCode {

    BAD_REQUEST(HttpStatus.BAD_REQUEST.value(), 400, "잘못된 요청"),
    MISSING_REQUIRED_HEADER(HttpStatus.BAD_REQUEST.value(), 400, "필수 헤더가 누락되었습니다"),
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), 500, "서버 에러");

    private final Integer httpStatus;
    private final Integer code;
    private final String message;
}
