package com.notFoundTomAndJerry.notFoundJerry.domain.game.dto.request;

import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.enums.RoleAssignType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게임 시작 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GameStartRequest {

    // 방 ID
    @NotNull(message = "방 ID는 필수입니다.")
    private Long roomId;

    // 역할 배치 방식 (RANDOM / MANUAL)
    @NotNull(message = "역할 배치 방식은 필수입니다.")
    private RoleAssignType roleAssignment;

    // 경찰 비율 (RANDOM일 때 사용, 선택)
    private Double policeRatio;
}
