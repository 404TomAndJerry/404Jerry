package com.notFoundTomAndJerry.notFoundJerry.domain.game.dto.request;

import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.enums.EndReason;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게임 종료 요청 DTO
 * 
 * 승리 팀은 종료 사유에 따라 자동으로 결정됨:
 * - ALL_THIEVES_CAUGHT → POLICE 승리
 * - TIME_OVER → THIEF 승리
 * - HOST_FORCED → POLICE 승리 (기본)
 * 
 * 사용 예시:
 * 1. 수동 종료 (방장): endReason=ALL_THIEVES_CAUGHT
 * 2. 자동 종료 (타임오버): endReason=TIME_OVER
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GameEndRequest {

    // 종료 사유 (승리 팀은 자동 결정)
    @NotNull(message = "종료 사유는 필수입니다.")
    private EndReason endReason;
}
