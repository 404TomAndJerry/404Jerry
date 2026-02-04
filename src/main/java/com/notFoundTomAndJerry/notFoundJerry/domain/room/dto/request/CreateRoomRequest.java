package com.notFoundTomAndJerry.notFoundJerry.domain.room.dto.request;

import com.notFoundTomAndJerry.notFoundJerry.domain.room.entity.enums.RoleAssignMode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CreateRoomRequest {
    private String title;
    private Long locationId;
    private Integer maxPlayers;
    private Integer policeCount;
    private Integer thiefCount;
    private Integer playTime;
    private LocalDateTime startTime;
    private RoleAssignMode roleAssignMode;
}
