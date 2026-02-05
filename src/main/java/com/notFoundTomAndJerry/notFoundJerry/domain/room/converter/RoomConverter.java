package com.notFoundTomAndJerry.notFoundJerry.domain.room.converter;

import com.notFoundTomAndJerry.notFoundJerry.domain.room.dto.response.JoinRoomResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.dto.response.ParticipantDetailResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.dto.response.RoomDetailResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.dto.response.RoomListResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.entity.Room;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.entity.RoomParticipant;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.BusinessException;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.domain.RoomErrorCode;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RoomConverter {

    /**
     * 방 목록 조회 DTO (닉네임 없음)
     */
    public RoomListResponse toListResponse(Room room) {
        if (room == null) return null;

        return RoomListResponse.builder()
                .roomId(room.getId())
                .locationId(room.getLocationId())
                .title(room.getTitle())
                .status(room.getStatus())
                .maxPlayers(room.getMaxPlayers())
                .currentParticipants(room.getCurrentParticipantCount())
                .startTime(room.getStartTime())
                .build();
    }

    /**
     * 방 상세 정보 DTO 변환 (닉네임 포함)
     *
     * @param room        조회된 방 엔티티 (participants Fetch Join 된 상태 권장)
     * @param nicknameMap (UserId -> Nickname) 매핑 맵
     */
    public RoomDetailResponse toDetailResponse(Room room, Map<Long, String> nicknameMap) {
        if (room == null) return null;

        // null safe 처리 (혹시 모를 상황 대비)
        Map<Long, String> safeNicknameMap = (nicknameMap != null) ? nicknameMap : Collections.emptyMap();

        return RoomDetailResponse.builder()
                .roomId(room.getId())
                .hostId(room.getHostId())
                .locationId(room.getLocationId())
                .title(room.getTitle())
                .maxPlayers(requestSafeMaxPlayers(room)) // null-safe getter 가정
                .currentCount(room.getCurrentParticipantCount()) // 엔티티 편의 메서드 활용
                .policeCount(room.getPoliceCount())
                .thiefCount(room.getThiefCount())
                .status(room.getStatus())
                .roleAssignMode(room.getRoleAssignMode())
                .startTime(room.getStartTime())
                .createdAt(room.getCreatedAt())
                // ✅ 참여자 리스트 변환 (닉네임 맵 전달)
                .participants(toParticipantResponses(room, safeNicknameMap))
                .build();
    }

    /**
     * 방 목록 조회용 간소화된 변환 (오버로딩)
     * - 목록 조회 시에는 닉네임 Map 없이 호출할 수도 있음 (기획에 따라 다름)
     * - 만약 목록에서도 참여자 닉네임이 필요하다면 위 메서드 사용
     */
    public RoomDetailResponse toDetailResponse(Room room) {
        // 닉네임 없이 호출 시 빈 맵 전달 ("알 수 없음" 등으로 처리됨)
        return toDetailResponse(room, Collections.emptyMap());
    }

    public JoinRoomResponse toJoinResponse(Room room, Long userId) {
        if (room == null) return null;

        // 방금 참가한 유저 찾기
        RoomParticipant participant = room.getParticipants().stream()
                .filter(p -> p.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(RoomErrorCode.PARTICIPANT_NOT_FOUND));

        return JoinRoomResponse.builder()
                .roomId(room.getId())
                .participantId(participant.getId())
                .userId(userId)
                .joinedAt(participant.getJoinedAt())
                .build();
    }

    // =================================================================
    // Private Helper Methods
    // =================================================================

    private List<ParticipantDetailResponse> toParticipantResponses(Room room, Map<Long, String> nicknameMap) {
        if (room.getParticipants() == null) {
            return Collections.emptyList();
        }

        return room.getParticipants().stream()
                .map(p -> ParticipantDetailResponse.builder()
                        .participantId(p.getId())
                        .userId(p.getUserId())
                        .nickname(nicknameMap.getOrDefault(p.getUserId(), "(알 수 없음)"))
                        .role(p.getRole())
                        .joinedAt(p.getJoinedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private Integer requestSafeMaxPlayers(Room room) {
        return room.getMaxPlayers() != null ? room.getMaxPlayers() : 0;
    }
}
