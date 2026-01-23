package com.notFoundTomAndJerry.notFoundJerry.domain.location.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LocationValidateResponse {

  private boolean isValid;
  private String message; // "유효한 좌표입니다" or 에러 메시지
}