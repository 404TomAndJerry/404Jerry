package com.notFoundTomAndJerry.notFoundJerry.global.exception;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum CommonErrorCode implements ApiCode {

    BAD_REQUEST(HttpStatus.BAD_REQUEST.value(), 400, "잘못된 요청"),
    MISSING_REQUIRED_HEADER(HttpStatus.BAD_REQUEST.value(), 400, "필수 헤더가 누락되었습니다"),
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), 500, "서버 에러"),

    // AUTH 도메인 TODO : 예시 일뿐 실제 서비스에 맞게 수정 필요
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED.value(), 401, "인증이 필요합니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED.value(), 401, "유효하지 않은 토큰입니다"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED.value(), 401, "토큰이 만료되었습니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN.value(), 403, "접근 권한이 없습니다"),
    ADMIN_REQUIRED(HttpStatus.FORBIDDEN.value(), 403, "관리자 권한이 필요합니다"),

    // CHAT 도메인 추가
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND.value(), 404, "채팅방을 찾을 수 없습니다."),
    CHAT_ROOM_NOT_FOUND_ID(404, 404, "채팅방을 찾을 수 없습니다. ID: %d"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND.value(), 404, "해당하는 유저를 찾지 못했습니다."),

    // CHAT 또는 REDIS 관련 도메인에 추가
    REDIS_PROCESSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), 500, "메시지 처리 중 오류가 발생했습니다.");

    private final Integer httpStatus;
    private final Integer code;
    private final String message;
}
