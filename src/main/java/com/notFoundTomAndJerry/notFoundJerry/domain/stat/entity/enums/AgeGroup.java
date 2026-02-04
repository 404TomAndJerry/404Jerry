package com.notFoundTomAndJerry.notFoundJerry.domain.stat.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum AgeGroup {
  TEENS("10s", 10, 19),
  TWENTIES("20s", 20, 29),
  THIRTIES("30s", 30, 39),
  FORTIES("40s", 40, 49),
  OVER_FIFTY("50s+", 50, 100); // 50대 이상 확장

  private final String key; // Redis Key Suffix ("20s")
  private final int minAge;
  private final int maxAge;

  // 나이(int)를 받아서 해당 그룹(Enum)을 찾는 로직
  public static AgeGroup fromAge(int age) {
    return Arrays.stream(values())
        .filter(group -> age >= group.minAge && age <= group.maxAge)
        .findFirst()
        .orElse(TEENS); // 예외 처리: 범위 밖이면 10대로 처리하거나 별도 처리
  }
}