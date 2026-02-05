package com.notFoundTomAndJerry.notFoundJerry.domain.stat.entity;

import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.enums.PlayerRole;
import com.notFoundTomAndJerry.notFoundJerry.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_history")
public class UserHistory {

  @Id
  private Long userId;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "user_id")
  private User user;

  // 1. 사용자별 통계 (Pre-aggregation)
  @Column(nullable = false)
  private Integer totalGames = 0;

  // 사용자 승률
  @Column(nullable = false)
  private Integer wins = 0;

  @Column(nullable = false)
  private Integer losses = 0;

  //탈주
  private Integer runawayCount = 0;

  // 2. 역할별 통계 (상세 분석)
  @Column(nullable = false)
  private Integer policeGames = 0;

  @Column(nullable = false)
  private Integer policeWins = 0; // 경찰로 이긴 횟수 추가 (승률 계산용)

  @Column(nullable = false)
  private Integer thiefGames = 0;

  @Column(nullable = false)
  private Integer thiefWins = 0;  // 도둑으로 이긴 횟수 추가

  @Builder
  public UserHistory(User user) {
    this.user = user;
  }

  // 비즈니스 로직: 게임 결과 반영 (반정규화 데이터 업데이트)
  public void updateGameResult(boolean isWinner, PlayerRole role, Boolean runawayCount) {
    this.totalGames++;

    if (isWinner) this.wins++;
    else this.losses++;

    if (PlayerRole.POLICE.equals(role)) {
      this.policeGames++;
      if (isWinner) this.policeWins++;
    } else if (PlayerRole.THIEF.equals(role)) {
      this.thiefGames++;
      if (isWinner) this.thiefWins++;
    }

    if(Boolean.TRUE.equals(runawayCount)){
      this.runawayCount++;
    }
  }

  // 승률 계산 (소수점 둘째자리까지 등 필요에 따라 로직 추가)
  public Double getWinRate() {
    if (totalGames == 0) return 0.0;
    return (double) wins / totalGames * 100.0;
  }
}