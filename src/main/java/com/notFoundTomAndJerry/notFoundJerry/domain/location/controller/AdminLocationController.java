package com.notFoundTomAndJerry.notFoundJerry.domain.location.controller;

import com.notFoundTomAndJerry.notFoundJerry.batch.LocationSyncBatch;
import com.notFoundTomAndJerry.notFoundJerry.batch.PublicDataCollector;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/locations")
@RequiredArgsConstructor
public class AdminLocationController {

  private final PublicDataCollector publicDataCollector;
  private final LocationSyncBatch locationSyncBatch; // 주소 동기화

  /**
   * [관리자] 공공데이터 수집 및 주소 동기화 통합 실행 호출: POST http://localhost:8080/api/admin/locations/sync
   */
  @PostMapping("/sync")
  public ResponseEntity<String> syncLocationData() {

    // 공공데이터로부터 기본 정보(이름, 좌표) 수집
    publicDataCollector.collect(1000);

    // 수집된 데이터를 바탕으로 카카오 API 호출하여 주소 및 행정구역 매핑
    locationSyncBatch.run();

    return ResponseEntity.ok("공공데이터 수집 및 카카오 주소 변환 작업이 모두 완료되었습니다.");
  }
}