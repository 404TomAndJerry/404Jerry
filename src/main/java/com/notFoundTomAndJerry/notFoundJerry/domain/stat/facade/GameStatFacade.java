package com.notFoundTomAndJerry.notFoundJerry.domain.stat.facade;

import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.enums.PlayerRole; // 상대방 Enum 사용
import com.notFoundTomAndJerry.notFoundJerry.domain.stat.entity.UserHistory;
import com.notFoundTomAndJerry.notFoundJerry.domain.stat.service.RedisStatService;
import com.notFoundTomAndJerry.notFoundJerry.domain.stat.service.UserHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameStatFacade {

  private final UserHistoryService userHistoryService;
  private final RedisStatService redisStatService;

  /**
   * [Game 도메인에서 호출]
   * 게임이 끝나면 이 메서드를 호출해주세요.
   * * @param userId      유저 ID
   * @param role        플레이한 역할 (POLICE, THIEF) - Enum 그대로 받기
   * @param isWinner    승리 여부
   * @param isRunaway   탈주 여부
   * @param regionName  게임 맵(지역) 이름 (통계용)
   * @param age         유저 나이 (통계용)
   */
  public void processGameStat(Long userId, PlayerRole role, boolean isWinner, boolean isRunaway, String regionName, int age) {
    try {
      // 2. RDB 업데이트 (UserHistory - 집계 데이터)
      UserHistory updatedHistory = userHistoryService.saveGameResult(
          userId,
          isWinner,
          role,
          isRunaway
      );

      // 3. Redis 업데이트 (랭킹/지역/나이 통계)
      redisStatService.updateAllStats(
          userId,
          updatedHistory.getWinRate(),
          regionName,
          age,
          role
      );

    } catch (Exception e) {
      log.error("[Stat Error] 통계 반영 실패 - userId: {}, gameId info missed", userId, e);
      // 통계 실패가 게임 결과 응답(GameResultResponse)을 막으면 안 되므로 예외를 삼킴(Log only)
    }
  }
}