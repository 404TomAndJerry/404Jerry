package com.notFoundTomAndJerry.notFoundJerry.domain.game.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 탈주 처리 응답 DTO
 * 서버에서 자동 호출, 탈주 시 패배 처리, runaway_logs 기록
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunawayResponse {

    // 탈주 처리 여부
    private Boolean runaway;

    // 탈주 시간
    private String runawayAt;
}
