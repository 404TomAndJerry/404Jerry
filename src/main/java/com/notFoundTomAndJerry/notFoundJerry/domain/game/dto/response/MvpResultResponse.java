package com.notFoundTomAndJerry.notFoundJerry.domain.game.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * MVP 결과 조회 응답 DTO
 * 등록 발생 시 투표 MVP 여부, 투표가 없을 경우 MVP 없음
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MvpResultResponse {

    // MVP 플레이어 목록 (동률 가능)
    private List<MvpPlayerDto> mvpPlayers;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MvpPlayerDto {
        // 사용자 ID
        private Long userId;

        // 닉네임
        private String nickname;
    }
}
