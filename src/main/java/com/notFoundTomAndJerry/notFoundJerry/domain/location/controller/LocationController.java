package com.notFoundTomAndJerry.notFoundJerry.domain.location.controller;

import com.notFoundTomAndJerry.notFoundJerry.batch.PublicDataCollector;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
public class LocationController {

  private final PublicDataCollector publicDataCollector;

  /**
   * 공공데이터 수동 동기화 API 호출 예: POST http://localhost:8080/api/v1/locations/sync
   */
  @PostMapping("/sync")
  public ResponseEntity<String> syncLocationData() {

    publicDataCollector.collect(1000);

    return ResponseEntity.ok("공공데이터 필터링 수집 및 자동 승인이 완료되었습니다.");
  }
}