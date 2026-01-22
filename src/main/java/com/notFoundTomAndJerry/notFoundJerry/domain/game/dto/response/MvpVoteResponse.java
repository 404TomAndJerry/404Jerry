package com.notFoundTomAndJerry.notFoundJerry.domain.game.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * MVP 투표 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MvpVoteResponse {

    // 투표 성공 여부
    private Boolean voted;
    
    // 투표 스킵 여부
    private Boolean skipped;
    
    // 응답 메시지 (선택)
    private String message;
}
