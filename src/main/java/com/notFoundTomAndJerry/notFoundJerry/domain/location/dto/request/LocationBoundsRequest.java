package com.notFoundTomAndJerry.notFoundJerry.domain.location.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 지도 화면의 사각형 범위(남서단-북동단) 좌표 정보를 담는 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LocationBoundsRequest {

  private Double minLat; // 남서단 위도
  private Double minLng; // 남서단 경도
  private Double maxLat; // 북동단 위도
  private Double maxLng; // 북동단 경도
}