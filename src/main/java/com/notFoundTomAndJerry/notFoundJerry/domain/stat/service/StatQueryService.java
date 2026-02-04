package com.notFoundTomAndJerry.notFoundJerry.domain.stat.service;

import com.notFoundTomAndJerry.notFoundJerry.domain.stat.dto.AgeStatResponseDto;
import com.notFoundTomAndJerry.notFoundJerry.domain.stat.dto.RankResponseDto;
import com.notFoundTomAndJerry.notFoundJerry.domain.stat.dto.RegionStatResponseDto;
import com.notFoundTomAndJerry.notFoundJerry.domain.stat.dto.UserStatResponseDto;
import com.notFoundTomAndJerry.notFoundJerry.domain.stat.entity.UserHistory;
import com.notFoundTomAndJerry.notFoundJerry.domain.stat.entity.enums.AgeGroup;
import com.notFoundTomAndJerry.notFoundJerry.domain.stat.repository.RedisStatRepository;
import com.notFoundTomAndJerry.notFoundJerry.domain.stat.repository.UserHistoryRepository;
import com.notFoundTomAndJerry.notFoundJerry.domain.user.entity.User;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.BusinessException;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.domain.StatErrorCode;
import com.notFoundTomAndJerry.notFoundJerry.domain.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatQueryService {

  private final UserRepository userRepository;
  private final RedisStatRepository redisStatRepository;
  private final UserHistoryRepository userHistoryRepository;

  private static final String KEY_RANK_WINRATE = "stats:rank:winrate";

  // 실시간 랭킹 Top 100 조회 (성능 최적화 적용)
  public List<RankResponseDto> getRealTimeRanks() {
    // 1. Redis에서 Top 100 ID와 점수 가져오기 (O(log N))
    Set<ZSetOperations.TypedTuple<String>> rankSet =
        redisStatRepository.getTopRanks(0,99);

    if (rankSet == null || rankSet.isEmpty()) {
      return Collections.emptyList();
    }

    // 2. ID 목록 추출
    // null이 아닌 ID만 추출
    List<Long> userIds = rankSet.stream()
        .map(ZSetOperations.TypedTuple::getValue)
        .filter(Objects::nonNull) // null 값 필터링
        .map(Long::valueOf)
        .toList();

    if (userIds.isEmpty()) return Collections.emptyList();

    // 3. DB Bulk 조회 (WHERE id IN (...)) -> N+1 문제 해결
    Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
        .collect(Collectors.toMap(User::getId, Function.identity()));

    // 4. Redis 순서대로 DTO 조립
    AtomicInteger rankCounter = new AtomicInteger(1);

    return rankSet.stream()
        .filter(t -> t.getValue() != null) // 안전장치
        .map(tuple -> {
          Long id = Long.valueOf(tuple.getValue());
          User user = userMap.getOrDefault(id, User.builder().nickname("Unknown").build());

          // Score가 null이면 0.0 처리
          Double winRate = tuple.getScore() != null ? tuple.getScore() : 0.0;

          return RankResponseDto.builder()
              .rank(rankCounter.getAndIncrement())
              .nickname(user.getNickname())
              .winRate(winRate)
              .build();
        })
        .toList();
  }

  // 2. 지역 통계 조회
  public List<RegionStatResponseDto> getTopRegionStats() {
    // RedisRepository에서 Top 5 조회 (0 ~ 4)
    Set<ZSetOperations.TypedTuple<String>> regionSet =
        redisStatRepository.getTopRegions(0, 4);

    if (regionSet == null || regionSet.isEmpty()) return Collections.emptyList();

    return regionSet.stream()
        .filter(tuple -> tuple.getValue() != null) // 1. 값이 null인 경우 제외
        .map(tuple -> {
          // 2. Score(Double) -> Long 안전하게 변환
          Double score = tuple.getScore();
          long playCount = (score != null) ? score.longValue() : 0L;

          return RegionStatResponseDto.builder()
              .regionName(tuple.getValue())
              .playCount(playCount) // 이제 Long 타입이므로 에러 없음
              .build();
        })
        .toList();
  }

  //3. 나이별
  public List<AgeStatResponseDto> getAgeStats() {
    List<AgeStatResponseDto> response = new ArrayList<>();

    // Enum에 정의된 모든 연령대를 자동으로 순회
    for (AgeGroup group : AgeGroup.values()) {
      String ageKey = group.getKey(); // "10s", "20s"...

      // RedisRepository에서 Double로 받아옴
      Double policeDouble = redisStatRepository.getAgeRoleCount(ageKey, "POLICE");
      Double thiefDouble = redisStatRepository.getAgeRoleCount(ageKey, "THIEF");

      // 3. DTO에 넣을 때 longValue()로 변환
      Long policeCount = policeDouble.longValue();
      Long thiefCount = thiefDouble.longValue();

      // 선호 역할 계산
      String preferred = calculatePreferredRole(policeCount, thiefCount);

      response.add(AgeStatResponseDto.builder()
          .ageGroup(ageKey)
          .policeCount(policeCount)
          .thiefCount(thiefCount)
          .preferredRole(preferred)
          .build());
    }
    return response;
  }

  // 특정 유저의 상세 통계 조회
  public UserStatResponseDto getUserStats(Long userId) {
    UserHistory history = userHistoryRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(StatErrorCode.USER_HISTORY_NOT_FOUND));
    return UserStatResponseDto.from(history);
  }

  // 비즈니스 로직 분리 (가독성 향상)
  private String calculatePreferredRole(Long police, Long thief) {
    if (police == 0 && thief == 0) return "NONE";
    return police >= thief ? "POLICE" : "THIEF";
  }
}