package com.notFoundTomAndJerry.notFoundJerry.domain.stat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RegionStatResponseDto {

  private String regionName;  // "Seoul"
  private Long playCount;     // 1540 (Redis Score는 Double이지만 DTO는 Long으로 변환)
}