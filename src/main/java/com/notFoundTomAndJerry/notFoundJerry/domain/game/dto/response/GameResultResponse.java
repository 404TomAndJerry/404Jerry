package com.notFoundTomAndJerry.notFoundJerry.domain.game.dto.response;

import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.enums.PlayerRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 게임 종료 후 하나의 게임 결과를 표현하는 결과 객체
 * 승리 팀 정보, 게임 종료 사유, MVP 사용자 닉네임 목록, 게임 결과 및 통계 저장 데이터 포함
 * 엔티티들을 조합하여 생성:
 * - Game: 게임 기본 정보
 * - GameResult: 플레이어별 승패 결과
 * - GamePlayer: 플레이어 역할 및 MVP 정보
 * - MvpVote: MVP 투표 결과
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameResultResponse {

    // 게임 ID
    private Long gameId;

    // 승리 팀 역할
    private PlayerRole winnerTeam;

    // 게임 종료 사유 (ex 타임 오버, 경찰팀 팀원 전원 탈주, 도둑팀 인원 전원 체포 등)
    private String endReason;

    // MVP로 선정된 사용자 닉네임 목록 (동률 MVP 허용을 위한 리스트 구조)
    private List<String> mvpUserNicknames;

    // 플레이어별 결과 정보
    private List<PlayerResultDto> playerResults;

    // 플레이어별 게임 결과 정보
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlayerResultDto {
        // 사용자 ID
        private Long userId;

        // 닉네임
        private String nickname;

        // 역할
        private PlayerRole role;

        // 승리 여부
        private boolean isWinner;

        // MVP 여부
        private boolean isMvp;
    }
}
