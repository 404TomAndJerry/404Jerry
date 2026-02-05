package com.notFoundTomAndJerry.notFoundJerry.domain.room.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LocationWithRoomCountResponse {

  private Long locationId;      // 장소 고유 ID
  private String parkNm;        // 공원 이름
  private Double latitude;      // 위도
  private Double longitude;     // 경도
  private String address;       // 주소
  private Long waitingRoomCount; // 해당 장소의 WAITING 상태 방 개수
  private Long playingRoomCount; // 해당 장소의 RUNNING 상태 방 개수
}