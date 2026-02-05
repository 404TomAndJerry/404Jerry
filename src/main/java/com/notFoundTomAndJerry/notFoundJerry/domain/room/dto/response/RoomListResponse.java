package com.notFoundTomAndJerry.notFoundJerry.domain.room.dto.response;

import com.notFoundTomAndJerry.notFoundJerry.domain.room.entity.enums.RoomStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RoomListResponse {
    private Long roomId;
    private Long locationId;
    private String title;
    private RoomStatus status;
    private Integer maxPlayers;
    private Integer currentParticipants;
    private LocalDateTime startTime;
}
