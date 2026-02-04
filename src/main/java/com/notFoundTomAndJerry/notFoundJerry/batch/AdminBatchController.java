package com.notFoundTomAndJerry.notFoundJerry.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/admin/batch")
@RequiredArgsConstructor
public class AdminBatchController {

  private final PublicDataCollector collector;
  private final LocationSyncBatch syncBatch;

  @PostMapping("/run")
  public String runBatch() {
    // 공공데이터에서 좌표 가져오기 (필터링 된 거)
    collector.collect(1000);
    // 가져온 좌표로 카카오 API 호출 후 주소 채우기
    syncBatch.run();

    return "배치 작업이 수동으로 실행되었습니다.";
  }
}