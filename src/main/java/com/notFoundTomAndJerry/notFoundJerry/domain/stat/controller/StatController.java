package com.notFoundTomAndJerry.notFoundJerry.domain.stat.controller;

import com.notFoundTomAndJerry.notFoundJerry.domain.stat.dto.AgeStatResponseDto;
import com.notFoundTomAndJerry.notFoundJerry.domain.stat.dto.RankResponseDto;
import com.notFoundTomAndJerry.notFoundJerry.domain.stat.dto.RegionStatResponseDto;
import com.notFoundTomAndJerry.notFoundJerry.domain.stat.dto.UserStatResponseDto;
import com.notFoundTomAndJerry.notFoundJerry.domain.stat.service.StatQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Statistics API", description = "전적, 랭킹, 통계 분석 데이터를 제공하는 API")
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatController {

  private final StatQueryService statQueryService;

  /**
   * 1. 실시간 전체 랭킹 조회 (TOP 100)
   * 시나리오: 메인 페이지나 '랭킹' 탭 진입 시 호출
   */
  @Operation(summary = "실시간 승률 랭킹 TOP 100 조회", description = "Redis ZSet을 기반으로 실시간 승률 순위를 반환합니다.")
  @GetMapping("/ranks")
  public ResponseEntity<List<RankResponseDto>> getRealTimeRanks() {
    // 서비스 계층에서 이미 N+1 문제가 해결된 DTO 리스트를 반환함
    return ResponseEntity.ok(statQueryService.getRealTimeRanks());
  }

  /**
   * 2. 지역별 인기 지역 통계 (TOP 5)
   * 시나리오: '핫 플레이스' 지도 시각화 시 호출
   */
  @Operation(summary = "인기 플레이 지역 TOP 5 조회", description = "가장 게임이 많이 열린 지역 5곳을 반환합니다.")
  @GetMapping("/regions")
  public ResponseEntity<List<RegionStatResponseDto>> getTopRegions() {
    return ResponseEntity.ok(statQueryService.getTopRegionStats());
  }

  /**
   * 3. 연령대별 역할 선호도 분석
   * 시나리오: '통계 분석' 페이지에서 차트(Bar Chart) 그릴 때 사용
   */
  @Operation(summary = "연령대별 역할 선호도 조회", description = "10대~40대+ 각 연령대별 경찰/도둑 플레이 비율을 반환합니다.")
  @GetMapping("/ages")
  public ResponseEntity<List<AgeStatResponseDto>> getAgeStats() {
    return ResponseEntity.ok(statQueryService.getAgeStats());
  }

  /**
   * 4. 내 전적 (또는 특정 유저 전적) 상세 조회
   * 시나리오: 마이페이지 또는 유저 프로필 클릭 시
   * 요구사항: 사용자별 총 게임 수, 승/패 횟수, 승률(%) 데이터 제공
   */
  @Operation(summary = "유저 상세 전적 조회", description = "총 게임 수, 승률, 역할별 플레이 횟수 등 상세 정보를 조회합니다.")
  @GetMapping("/{userId}")
  public ResponseEntity<UserStatResponseDto> getUserStats(@PathVariable Long userId) {
    return ResponseEntity.ok(statQueryService.getUserStats(userId));
  }
}