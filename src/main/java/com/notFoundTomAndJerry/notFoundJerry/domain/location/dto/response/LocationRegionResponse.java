package com.notFoundTomAndJerry.notFoundJerry.domain.location.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LocationRegionResponse {

  // 좌표가 속한 행정구역 이름 반환
  private String regionName; // ex) 강남구
}