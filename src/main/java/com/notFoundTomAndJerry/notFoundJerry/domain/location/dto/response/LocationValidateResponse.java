package com.notFoundTomAndJerry.notFoundJerry.domain.location.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LocationValidateResponse {

  // 방 생성 전 선택한 위치가 마스터 데이터상 유효한지 알려줌
  private boolean isValid;
  private String message; // "유효한 좌표입니다" or 에러 메시지
}