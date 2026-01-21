package com.notFoundTomAndJerry.notFoundJerry.domain.game.dto.response;

import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.enums.GameStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 게임 상태 조회를 위한 Response DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameStatusResponse {

    // 게임 ID
    private Long gameId;

    // 게임 상태
    private GameStatus status;

    // 게임 시작 시간
    private LocalDateTime startedAt;

    // 게임 종료 시간
    private LocalDateTime endedAt;

    // 방 ID
    private Long roomId;
}
