package com.notFoundTomAndJerry.notFoundJerry.domain.game.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게임 종료 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameEndResponse {

    // 게임 ID
    private Long gameId;

    // 게임 상태 ("FINISHED")
    private String status;

    // 종료 시간
    private String endedAt;
}
