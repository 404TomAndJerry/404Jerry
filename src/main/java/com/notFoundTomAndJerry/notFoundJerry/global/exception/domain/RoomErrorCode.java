package com.notFoundTomAndJerry.notFoundJerry.global.exception.domain;

import com.notFoundTomAndJerry.notFoundJerry.global.exception.ApiCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RoomErrorCode implements ApiCode {
    // 400 Bad Request
    INVALID_START_TIME(HttpStatus.BAD_REQUEST.value(), 4001, "시작 시간은 현재 이후여야 합니다."),
    INVALID_MIN_PLAYERS(HttpStatus.BAD_REQUEST.value(), 4002, "최소 인원은 6명 이상이어야 합니다."),
    INVALID_ROLE_COUNT(HttpStatus.BAD_REQUEST.value(), 4003, "경찰과 도둑 수의 합은 전체 정원과 일치해야 합니다."),
    ALREADY_JOINED(HttpStatus.BAD_REQUEST.value(), 4004, "이미 참가 중인 방입니다."),
    ROOM_FULL(HttpStatus.BAD_REQUEST.value(), 4005, "방의 정원이 가득 찼습니다."),
    INVALID_ROOM_STATUS(HttpStatus.BAD_REQUEST.value(), 4006, "현재 상태에서는 요청을 처리할 수 없습니다."),
    ROLE_ASSIGN_NOT_ALLOWED(HttpStatus.BAD_REQUEST.value(), 4007, "수동 역할 배정 모드가 아닙니다."),
    ROLE_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST.value(), 4008, "해당 역할의 정원이 초과되었습니다."),
    ONLY_HOST_ALLOWED(HttpStatus.BAD_REQUEST.value(), 4009, "방장만 수행할 수 있는 작업입니다."),

    // 404 Not Found
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND.value(), 4041, "존재하지 않는 방입니다."),
    PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND.value(), 4042, "참가자 정보를 찾을 수 없습니다."),
    LOCATION_NOT_FOUND(HttpStatus.NOT_FOUND.value(), 4043, "존재하지 않는 지역입니다.");

    private final Integer httpStatus;
    private final Integer code;
    private final String message;
}
