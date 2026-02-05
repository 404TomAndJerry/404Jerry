package com.notFoundTomAndJerry.notFoundJerry.domain.room.dto.request;

import com.notFoundTomAndJerry.notFoundJerry.domain.room.entity.enums.RoomStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "방 목록 조회 요청")
public class RoomListRequest {
    @Schema(
            description = "지역 ID (선택사항)",
            example = "1",
            nullable = true
    )
    private Long locationId;
    @Schema(
            description = "방 상태 (선택사항)",
            example = "WAITING",
            nullable = true
    )
    private RoomStatus status;
}
