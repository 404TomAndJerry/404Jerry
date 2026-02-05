package com.notFoundTomAndJerry.notFoundJerry.domain.room.controller;

import com.notFoundTomAndJerry.notFoundJerry.domain.room.dto.request.ChangeRoleRequest;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.dto.request.CreateRoomRequest;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.dto.request.RoomListRequest;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.dto.response.JoinRoomResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.dto.response.LocationWithRoomCountResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.dto.response.RoomDetailResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.dto.response.RoomListResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.service.RoomService;
import com.notFoundTomAndJerry.notFoundJerry.global.security.CustomPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Tag(name = "Room API", description = "방 관리 API")
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    /**
     * 방 생성
     */
    @PostMapping
    @Operation(summary = "방 생성", description = "새로운 방을 생성합니다")
    public ResponseEntity<Long> createRoom(
            @AuthenticationPrincipal CustomPrincipal principal,
            @Valid @RequestBody CreateRoomRequest request
    ) {
        Long roomId = roomService.createRoom(principal.getUserId(), request);
        return ResponseEntity
                .created(URI.create("/api/rooms/" + roomId))
                .body(roomId);
    }

    /**
     * 방 목록 조회
     */
    @GetMapping
    @Operation(summary = "방 목록 조회", description = "필터 및 페이징을 적용하여 방 목록을 조회합니다")
    public ResponseEntity<Page<RoomListResponse>> listRooms(
            @ParameterObject RoomListRequest request,
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<RoomListResponse> response = roomService.listRooms(request, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 방 상세 조회
     */
    @GetMapping("/{roomId}")
    @Operation(summary = "방 상세 조회", description = "방의 상세 정보와 참여자 정보를 조회합니다")
    public ResponseEntity<RoomDetailResponse> getRoom(@PathVariable Long roomId) {
        RoomDetailResponse response = roomService.getRoom(roomId);
        return ResponseEntity.ok(response);
    }

    /**
     * 방 참가
     */
    @PostMapping("/{roomId}/participants")
    @Operation(summary = "방 참가", description = "방에 참가합니다")
    public ResponseEntity<JoinRoomResponse> joinRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomPrincipal principal
    ) {
        JoinRoomResponse response = roomService.joinRoom(roomId, principal.getUserId());
        return ResponseEntity.ok(response);
    }

    /**
     * 방 나가기( 호스트가 나가면 방삭제 )
     */
    @DeleteMapping("/{roomId}/participants/me")
    @Operation(summary = "방 나가기", description = "방에서 나갑니다. 방장이 나가면 방이 삭제됩니다")
    public ResponseEntity<Void> leaveRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomPrincipal principal
    ) {
        roomService.leaveRoom(roomId, principal.getUserId());
        return ResponseEntity.noContent().build();
    }

    /**
     * 역할 변경
     */
    @PatchMapping("/{roomId}/participants/{targetUserId}/role")
    @Operation(summary = "역할 변경", description = "참여자의 역할을 변경합니다 (MANUAL 모드만 가능)")
    public ResponseEntity<Void> changeRole(
            @PathVariable Long roomId,
            @PathVariable Long targetUserId,
            @Valid @RequestBody ChangeRoleRequest request
    ) {
        roomService.changeRole(roomId, targetUserId, request.getRole());
        return ResponseEntity.ok().build();
    }

    /**
     * 지도 마커 조회 API 현재 지도 영역(Bounding Box) 내의 장소별 대기/게임 중인 방 개수를 반환
     */
    @GetMapping("/map-markers")
    public List<LocationWithRoomCountResponse> getMapMarkers(
        @RequestParam(name = "minLat", required = false, defaultValue = "30.0") Double minLat,
        @RequestParam(name = "maxLat", required = false, defaultValue = "45.0") Double maxLat,
        @RequestParam(name = "minLng", required = false, defaultValue = "120.0") Double minLng,
        @RequestParam(name = "maxLng", required = false, defaultValue = "135.0") Double maxLng
    ) {
        return roomService.getMapMarkers(minLat, maxLat, minLng, maxLng);
    }

}
