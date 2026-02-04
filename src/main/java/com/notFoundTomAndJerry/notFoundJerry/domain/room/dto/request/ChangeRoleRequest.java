package com.notFoundTomAndJerry.notFoundJerry.domain.room.dto.request;

import com.notFoundTomAndJerry.notFoundJerry.domain.room.entity.enums.ParticipantRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChangeRoleRequest {

    @NotNull(message = "변경할 역할은 필수입니다.")
    private ParticipantRole role;
}
