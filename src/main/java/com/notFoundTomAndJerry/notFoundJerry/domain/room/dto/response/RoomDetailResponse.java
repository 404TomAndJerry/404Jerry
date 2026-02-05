package com.notFoundTomAndJerry.notFoundJerry.domain.room.dto.response;

import com.notFoundTomAndJerry.notFoundJerry.domain.room.entity.enums.RoleAssignMode;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.entity.enums.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomDetailResponse {
    private Long roomId;
    private Long hostId;
    private Long locationId;
    private String title;
    private Integer maxPlayers;
    private Integer currentCount; // 현재 인원
    private Integer policeCount;
    private Integer thiefCount;
    private RoomStatus status;
    private RoleAssignMode roleAssignMode;
    private LocalDateTime startTime;
    private LocalDateTime createdAt;

    // 참여자 목록
    private List<ParticipantDetailResponse> participants;
}