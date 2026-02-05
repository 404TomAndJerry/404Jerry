package com.notFoundTomAndJerry.notFoundJerry.domain.location.controller;

import com.notFoundTomAndJerry.notFoundJerry.domain.location.dto.response.LocationResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.location.service.LocationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

  private final LocationService locationService;

  /**
   * [프론트엔드] 유효 장소 목록 전체 조회 호출: GET http://localhost:8080/api/locations * @return 지도 마커 렌더링을 위한 장소
   * 데이터 리스트
   */
  @GetMapping
  public ResponseEntity<List<LocationResponse>> getLocations() {
    return ResponseEntity.ok(locationService.getAllAvailableLocations());
  }

  /**
   * [조회] 특정 위치 기준 반경 내 장소 조회 호출 예시: GET
   * http://localhost:8080/api/locations/nearby?lat=37.654&lng=127.056&radius=3000
   *
   * @param lat    위도
   * @param lng    경도
   * @param radius 반경(미터, 기본값 3km)
   */
  @GetMapping("/nearby")
  public ResponseEntity<List<LocationResponse>> getNearbyLocations(
      @RequestParam Double lat,
      @RequestParam Double lng,
      @RequestParam(defaultValue = "3000") Double radius) {

    List<LocationResponse> responses = locationService.getLocationsByRadius(lat, lng, radius);
    return ResponseEntity.ok(responses);
  }
}