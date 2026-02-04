package com.notFoundTomAndJerry.notFoundJerry.domain.stat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RankResponseDto {

  private int rank;           // 순위
  private String nickname;    // 유저 닉네임
  private Double winRate;     // 승률
}
