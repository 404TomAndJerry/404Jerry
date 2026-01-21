package com.notFoundTomAndJerry.notFoundJerry.domain.game.dto.request;

import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.enums.PlayerRole;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 플레이어별 역할 지정 정보 전달 DTO
 * 일부 플레이어만 지정된 경우, 나머지는 랜덤 배치 기준으로 사용됨
 * 역할 비율 검증(validateRoleRatio) 시에도 함께 사용됨
 * Map 구조:
 * - Key: userId (Long, 양수, null 불가)
 * - Value: PlayerRole (POLICE 또는 THIEF, null 불가)
 * - 최소 6명 이상이어야 함
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoleAssignRequest {

    // 사용자 ID와 역할 매핑 (최소 6명)
    @NotNull(message = "플레이어 역할 정보는 필수입니다.")
    @NotEmpty(message = "최소 1명 이상의 역할을 지정해야 합니다.")
    @Size(min = 6, message = "플레이어는 6명 이상이어야 합니다.")
    private Map<Long, PlayerRole> playerRoles;
}
