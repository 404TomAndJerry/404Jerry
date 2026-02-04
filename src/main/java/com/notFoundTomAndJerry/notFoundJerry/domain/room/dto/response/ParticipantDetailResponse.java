package com.notFoundTomAndJerry.notFoundJerry.domain.room.dto.response;


import com.notFoundTomAndJerry.notFoundJerry.domain.room.entity.enums.ParticipantRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ParticipantDetailResponse {
    private Long participantId;
    private Long userId;
    private String nickname;
    private ParticipantRole role;
    private LocalDateTime joinedAt;
}