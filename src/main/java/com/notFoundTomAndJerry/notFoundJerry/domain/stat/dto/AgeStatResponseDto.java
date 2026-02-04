package com.notFoundTomAndJerry.notFoundJerry.domain.stat.dto;

import com.notFoundTomAndJerry.notFoundJerry.domain.stat.entity.UserHistory;
import com.notFoundTomAndJerry.notFoundJerry.domain.stat.entity.enums.AgeGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AgeStatResponseDto {

  private String ageGroup;      // "20s"
  private Long policeCount;   // 120
  private Long thiefCount;    // 80
  private String preferredRole; // "POLICE"
}
