package com.notFoundTomAndJerry.notFoundJerry.domain.stat.service;

import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.enums.PlayerRole;
import com.notFoundTomAndJerry.notFoundJerry.domain.stat.entity.enums.AgeGroup;
import com.notFoundTomAndJerry.notFoundJerry.domain.stat.repository.RedisStatRepository;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.BusinessException;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.domain.StatErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisStatService {

  private final RedisStatRepository redisStatRepository;

  /**
   * Facade에서 호출하는 통합 업데이트 메서드
   */
  @Async(value = "statTaskExecutor")
  public void updateAllStats(Long userId, Double winRate, String regionName, Integer age, PlayerRole role) {
    try {
      // 1. 실시간 승률 랭킹
      redisStatRepository.updateWinRate(userId, winRate);

      // 2. 지역 통계
      if (regionName != null && !regionName.isBlank()) {
        redisStatRepository.incrementRegionPlay(regionName);
      }

      // 3. 연령대별 역할 선호도
      AgeGroup group = AgeGroup.fromAge(age);
      redisStatRepository.incrementAgeRolePlay(group.getKey(), role);

    } catch (Exception e) {
      // 비동기 로직이므로 메인 흐름에 영향을 주지 않고 에러 로그만 남김
      log.error("Failed to update redis stats for user: {}", userId, e);
      throw new BusinessException(StatErrorCode.STAT_UPDATE_FAILED);
    }
  }
}