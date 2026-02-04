package com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.enums;

/**
 * 게임 종료 사유 (승리 팀 자동 결정)
 *
 * 종료 사유에 따라 승리 팀이 자동으로 결정됨
 * - ALL_THIEVES_CAUGHT: 경찰 승리
 * - TIME_OVER: 도둑 승리
 * - HOST_FORCED: 경찰 승리 (기본값)
 */
public enum EndReason {
    ALL_THIEVES_CAUGHT("도둑팀 전원 체포", PlayerRole.POLICE),
    TIME_OVER("타임 오버", PlayerRole.THIEF),
    HOST_FORCED("방장 강제 종료", null);

    private final String description;
    private final PlayerRole winner;

    EndReason(String description, PlayerRole winner) {
        this.description = description;
        this.winner = winner;
    }

    public String getDescription() {
        return description;
    }

    public PlayerRole getWinner() {
        return winner;
    }
}