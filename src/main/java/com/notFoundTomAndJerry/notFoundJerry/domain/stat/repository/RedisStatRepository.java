package com.notFoundTomAndJerry.notFoundJerry.domain.stat.repository;

import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.enums.PlayerRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RedisStatRepository {

  private final StringRedisTemplate redisTemplate;

  // 키 관리를 위한 상수 정의 (유지보수성 향상)
  private static final String KEY_RANK_WINRATE = "stats:rank:winrate";
  private static final String KEY_RANK_REGION = "stats:rank:region";
  private static final String KEY_PREFIX_AGE = "stats:age:"; // 접두사

  // ==========================================
  //  Write Operations (데이터 적재)
  // ==========================================

  /**
   * 1. 실시간 승률 랭킹 업데이트 (ZSet)
   * 시나리오: 게임 종료 시 유저의 최신 승률로 점수를 덮어씀 (Upsert)
   */
  public void updateWinRate(Long userId, Double winRate) {
    // ZADD stats:rank:winrate {winRate} {userId}
    redisTemplate.opsForZSet().add(KEY_RANK_WINRATE, String.valueOf(userId), winRate);
  }

  /**
   * 2. 지역별 인기도 증가 (ZSet)
   * 시나리오: 해당 지역에서 게임이 열릴 때마다 점수(횟수) +1
   */
  public void incrementRegionPlay(String regionName) {
    // ZINCRBY stats:rank:region 1 {regionName}
    redisTemplate.opsForZSet().incrementScore(KEY_RANK_REGION, regionName, 1.0);
  }

  /**
   * 3. 연령대별 역할 선호도 증가 (Hash)
   * 시나리오: 20대가 경찰을 플레이하면 -> stats:age:20s 해시의 POLICE 필드 +1
   */
  public void incrementAgeRolePlay(String ageGroup, PlayerRole role) {
    // HINCRBY stats:age:{ageGroup} {role} 1
    String key = KEY_PREFIX_AGE + ageGroup;
    redisTemplate.opsForHash().increment(key, role, 1);
  }

  // ==========================================
  //  Read Operations (데이터 조회)
  // ==========================================

  /**
   * 전체 랭킹 조회 (Top N)
   * 사용처: 리더보드 페이지
   */
  public Set<ZSetOperations.TypedTuple<String>> getTopRanks(long start, long end) {
    // ZREVRANGE (내림차순, 점수 높은 순)
    return redisTemplate.opsForZSet().reverseRangeWithScores(KEY_RANK_WINRATE, start, end);
  }

  /**
   * 인기 지역 조회 (Top N)
   * 사용처: 메인 페이지 '핫 플레이스'
   */
  public Set<ZSetOperations.TypedTuple<String>> getTopRegions(long start, long end) {
    return redisTemplate.opsForZSet().reverseRangeWithScores(KEY_RANK_REGION, start, end);
  }

  /**
   * 특정 연령대의 역할별 플레이 횟수 조회
   * 사용처: 통계 분석 페이지 (승률 차트 등)
   */
  public Double getAgeRoleCount(String ageGroup, String role) {
    try {
      // 1. Redis에서 값 조회
      Object value = redisTemplate.opsForHash().get(ageGroup, role);

      if (value == null) {
        return 0.0;
      }

      // 2. 데이터 타입 변환 시도
      return Double.parseDouble(value.toString());

    } catch (NumberFormatException e) {
      // 데이터가 숫자로 변환될 수 없는 형식일 경우
      log.error("Redis 데이터 변환 에러 - Key: {}, Role: {}, Value: {}", ageGroup, role, e.getMessage());
      return 0.0;
    } catch (Exception e) {
      // Redis 연결 오류 등 기타 예상치 못한 예외 발생 시
      log.error("Redis 조회 중 예상치 못한 에러 발생 - Key: {}", ageGroup, e);
      return 0.0;
    }
  }
}