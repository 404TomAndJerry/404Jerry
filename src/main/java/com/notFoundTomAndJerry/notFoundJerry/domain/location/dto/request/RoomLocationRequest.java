package com.notFoundTomAndJerry.notFoundJerry.domain.location.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 거리 계산 대상인 특정 방의 위치 좌표를 담는 DTO
 */
@Getter
@NoArgsConstructor
public class RoomLocationRequest {

  private Double latitude;
  private Double longitude;
}