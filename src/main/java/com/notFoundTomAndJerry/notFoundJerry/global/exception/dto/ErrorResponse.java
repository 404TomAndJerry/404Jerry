package com.notFoundTomAndJerry.notFoundJerry.global.exception.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {

    private final Integer code;   // 서비스 내부 코드 (httpStatus와 분리)
    private final String message; // 클라이언트 메시지
}
