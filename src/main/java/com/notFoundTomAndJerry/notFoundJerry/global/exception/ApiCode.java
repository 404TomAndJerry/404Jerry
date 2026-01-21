package com.notFoundTomAndJerry.notFoundJerry.global.exception;

public interface ApiCode {

    Integer getHttpStatus();  // HTTP status (200, 201, 400 ...)

    Integer getCode();        // 서비스 내부 코드(원하면 HTTP와 동일하게 써도 됨)

    String getMessage();      // 사용자/클라에 내려줄 기본 메시지
}