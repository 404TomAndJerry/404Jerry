package com.notFoundTomAndJerry.notFoundJerry.domain.location.dto.response;

import com.notFoundTomAndJerry.notFoundJerry.domain.location.entity.Location;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LocationResponse {

  private Long id;
  private String parkNm;     // 공원 이름
  private String address;    // 변환된 전체 주소
  private String regionName; // 구 단위 (강남구, 서초구 등)
  private Double latitude;   // 위도 (Point.getY)
  private Double longitude;  // 경도 (Point.getX)

  public static LocationResponse from(Location location) {
    return LocationResponse.builder()
        .id(location.getId())
        .parkNm(location.getParkNm())
        .address(location.getAddress())
        .regionName(location.getRegionName())
        .latitude(location.getPoint().getY())
        .longitude(location.getPoint().getX())
        .build();
  }
}