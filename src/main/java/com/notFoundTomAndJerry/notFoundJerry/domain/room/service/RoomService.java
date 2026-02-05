package com.notFoundTomAndJerry.notFoundJerry.domain.room.service;

import com.notFoundTomAndJerry.notFoundJerry.domain.room.dto.request.CreateRoomRequest;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.dto.request.RoomListRequest;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.dto.response.JoinRoomResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.dto.response.RoomDetailResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.dto.response.RoomListResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.entity.enums.ParticipantRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoomService {

    /**
     * 방 생성
     */
    Long createRoom(Long hostUserId, CreateRoomRequest request);

    /**
     * 방 목록 조회 (필터 + 페이징)
     */
    Page<RoomListResponse> listRooms(RoomListRequest request, Pageable pageable);

    /**
     * 방 단건 조회 (상세)
     */
    RoomDetailResponse getRoom(Long roomId);

    /**
     * 방 참가
     */
    JoinRoomResponse joinRoom(Long roomId, Long userId);

    /**
     * 방 나가기 (방장이면 삭제 or 위임 정책 적용)
     */
    void leaveRoom(Long roomId, Long userId);

    /**
     * 역할 변경 (MANUAL 모드 전용)
     */
    void changeRole(Long roomId, Long userId, ParticipantRole newRole);
}