package com.notFoundTomAndJerry.notFoundJerry.global.exception.domain;

import com.notFoundTomAndJerry.notFoundJerry.global.exception.ApiCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum UserErrorCode implements ApiCode {

  USER_NOT_FOUND(HttpStatus.NOT_FOUND.value(), 404, "존재하지 않는 유저입니다."),
  NICKNAME_DUPLICATION(HttpStatus.BAD_REQUEST.value(), 400, "이미 사용 중인 닉네임입니다.");

  private final Integer httpStatus;
  private final Integer code;
  private final String message;
}