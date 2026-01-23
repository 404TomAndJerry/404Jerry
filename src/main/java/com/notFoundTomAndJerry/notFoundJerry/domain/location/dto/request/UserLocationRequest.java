package com.notFoundTomAndJerry.notFoundJerry.domain.location.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자의 현재 위치(기준점) 좌표를 담는 DTO
 */
@Getter
@NoArgsConstructor
public class UserLocationRequest {

  private Double latitude;
  private Double longitude;
}