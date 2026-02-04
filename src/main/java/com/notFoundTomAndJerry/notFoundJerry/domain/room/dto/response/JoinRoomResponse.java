package com.notFoundTomAndJerry.notFoundJerry.domain.room.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class JoinRoomResponse {
    private Long roomId;
    private Long participantId;
    private Long userId;
    private LocalDateTime joinedAt;
}
