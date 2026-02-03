package com.notFoundTomAndJerry.notFoundJerry.domain.game.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게임 시작 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameStartResponse {

    // 생성된 게임 ID
    private Long gameId;

    // 게임 상태
    private String status;

    // 게임 시작 시간
    private String startedAt;

    // 경찰 인원 수
    private Integer policeCount;

    // 도둑 인원 수
    private Integer thiefCount;
}
