package com.notFoundTomAndJerry.notFoundJerry.domain.room.controller;

import com.notFoundTomAndJerry.notFoundJerry.domain.room.dto.request.ChangeRoleRequest;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.dto.request.CreateRoomRequest;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.dto.request.RoomListRequest;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.dto.response.JoinRoomResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.dto.response.RoomDetailResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    /**
     * 방 생성
     */
    @PostMapping
    public ResponseEntity<Long> createRoom(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @Valid @RequestBody CreateRoomRequest request
    ) {
        Long roomId = roomService.createRoom(userId, request);
        return ResponseEntity
                .created(URI.create("/api/rooms/" + roomId))
                .body(roomId);
    }

    /**
     * 방 목록 조회
     */
    @GetMapping
    public ResponseEntity<Page<RoomDetailResponse>> listRooms(
            @ModelAttribute RoomListRequest request,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<RoomDetailResponse> response = roomService.listRooms(request, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 방 상세 조회
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<RoomDetailResponse> getRoom(@PathVariable Long roomId) {
        RoomDetailResponse response = roomService.getRoom(roomId);
        return ResponseEntity.ok(response);
    }

    /**
     * 방 참가
     */
    @PostMapping("/{roomId}/participants")
    public ResponseEntity<JoinRoomResponse> joinRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        JoinRoomResponse response = roomService.joinRoom(roomId, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 방 나가기( 호스트가 나가면 방삭제 )
     */
    @DeleteMapping("/{roomId}/participants/me")
    public ResponseEntity<Void> leaveRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        roomService.leaveRoom(roomId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 역할 변경
     */
    @PatchMapping("/{roomId}/participants/{targetUserId}/role")
    public ResponseEntity<Void> changeRole(
            @PathVariable Long roomId,
            @PathVariable Long targetUserId,
            @Valid @RequestBody ChangeRoleRequest request
    ) {
        roomService.changeRole(roomId, targetUserId, request.getRole());
        return ResponseEntity.ok().build();
    }

}
