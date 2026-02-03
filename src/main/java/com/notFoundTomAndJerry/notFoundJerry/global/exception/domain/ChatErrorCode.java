package com.notFoundTomAndJerry.notFoundJerry.global.exception.domain;

import com.notFoundTomAndJerry.notFoundJerry.global.exception.ApiCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Slf4j
@Getter
public enum ChatErrorCode implements ApiCode {

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
