package com.notFoundTomAndJerry.notFoundJerry.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LocationScheduler {

  private final PublicDataCollector publicDataCollector;
  private final LocationSyncBatch locationSyncBatch;

  /**
   * 매주 월요일 새벽 3시에 실행되는 자동 업데이트 스케줄러 cron 표현식: 초 분 시 일 월 요일 "0 0 3 * * MON" : 매주 월요일 03시 00분 00초
   */
  @Scheduled(cron = "0 0 3 * * MON")
  public void weeklyLocationUpdate() {
    log.info("[자동 배치] 주간 데이터 동기화 작업을 시작합니다.");

    try {
      // 1단계: 공공데이터 수집 및 필터링 (수동으로 했던 /sync 호출)
      log.info("[1단계] 공공데이터 수집 시작");
      publicDataCollector.collect(1000);

      // 2단계: 카카오 API를 이용한 주소 변환 (수동으로 했던 /batch/run 호출)
      log.info("[2단계] 카카오 주소 변환 시작");
      locationSyncBatch.run();

      log.info("[자동 배치] 모든 작업이 성공적으로 완료되었습니다.");
    } catch (Exception e) {
      log.error("[자동 배치] 작업 중 에러 발생: {}", e.getMessage());
    }
  }
}