package com.notFoundTomAndJerry.notFoundJerry.domain.stat.dto;

import com.notFoundTomAndJerry.notFoundJerry.domain.stat.entity.UserHistory;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;


@Getter
@Builder
@AllArgsConstructor
public class UserStatResponseDto {

  private Long userId;
  private Integer totalGames;
  private Integer wins;
  private Integer losses;
  private String winRate; // "55.5%" 문자열 포맷팅

  // 역할별 상세
  private Integer policePlays;
  private Integer thiefPlays;
  private String preferredRole; // "POLICE"

  // Entity -> DTO 변환 메서드 (Factory Method)
  @NonNull
  public static UserStatResponseDto from(UserHistory history) {
    return UserStatResponseDto.builder()
        .userId(history.getUserId())
        .totalGames(history.getTotalGames())
        .wins(history.getWins())
        .losses(history.getLosses())
        .winRate(String.format("%.1f%%", history.getWinRate()))
        .policePlays(history.getPoliceGames())
        .thiefPlays(history.getThiefGames())
        .preferredRole(history.getPoliceGames() >= history.getThiefGames() ? "POLICE" : "THIEF")
        .build();
  }
}