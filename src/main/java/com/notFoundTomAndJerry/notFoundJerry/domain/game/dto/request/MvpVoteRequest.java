package com.notFoundTomAndJerry.notFoundJerry.domain.game.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * MVP 투표 요청 DTO
 * - targetUserId가 null이면 투표 스킵으로 처리
 * - skip 필드로 명확하게 스킵 여부를 표현할 수 있음
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MvpVoteRequest {

    // 투표자 ID (TODO: JWT 인증 통합 시 제거하고 토큰에서 추출) 추후 JWT 인증 통합 시 제거
    @NotNull(message = "투표자 ID는 필수입니다.")
    private Long voterId;

    // 투표 대상 사용자 ID (null이면 스킵)
    private Long targetUserId;
    
    // 투표 스킵 여부 (기본값: false)
    private Boolean skip;
}
