package com.notFoundTomAndJerry.notFoundJerry.domain.room.dto.request;

import com.notFoundTomAndJerry.notFoundJerry.domain.room.entity.enums.RoomStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RoomListRequest {
    private Long locationId;
    private RoomStatus status;
}
