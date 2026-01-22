package com.notFoundTomAndJerry.notFoundJerry.domain.game.dto.response;

import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.enums.PlayerRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게임 내 플레이어 역할 정보를 조회하기 위한 Response DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerRoleResponse {

    // 사용자 ID
    private Long userId;

    // 역할 (경찰 / 도둑)
    private PlayerRole role;

    // 닉네임
    private String nickname;
}
