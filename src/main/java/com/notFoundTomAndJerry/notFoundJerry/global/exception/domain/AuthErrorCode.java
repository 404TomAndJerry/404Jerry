package com.notFoundTomAndJerry.notFoundJerry.global.exception.domain;

import com.notFoundTomAndJerry.notFoundJerry.global.exception.ApiCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum AuthErrorCode implements ApiCode {

  EMAIL_DUPLICATION(HttpStatus.BAD_REQUEST.value(), 400, "이미 존재하는 이메일입니다."),
  LOGIN_FAILED(HttpStatus.UNAUTHORIZED.value(), 401, "아이디 또는 비밀번호가 잘못되었습니다."),
  INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED.value(), 401, "유효하지 않은 리프레시 토큰입니다."),
  USER_NOT_FOUND(HttpStatus.NOT_FOUND.value(), 404, "존재하지 않는 유저입니다.");

  private final Integer httpStatus;
  private final Integer code;
  private final String message;
}