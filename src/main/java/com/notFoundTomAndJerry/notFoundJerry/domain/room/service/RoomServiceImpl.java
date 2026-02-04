package com.notFoundTomAndJerry.notFoundJerry.domain.room.service;

import com.notFoundTomAndJerry.notFoundJerry.domain.location.repository.LocationRepository;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.converter.RoomConverter;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.dto.request.CreateRoomRequest;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.dto.request.RoomListRequest;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.dto.response.JoinRoomResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.dto.response.RoomDetailResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.entity.Room;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.entity.RoomParticipant;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.entity.enums.ParticipantRole;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.entity.enums.RoleAssignMode;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.repository.RoomRepository;
import com.notFoundTomAndJerry.notFoundJerry.domain.user.repository.UserRepository;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.BusinessException;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.domain.RoomErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final RoomConverter roomConverter;

    @Override
    @Transactional
    public Long createRoom(Long hostUserId, CreateRoomRequest request) {
        // [검증] 지역 존재 여부 확인 (주석 해제 시 사용)
        // validateLocationExists(request.getLocationId());

        RoleAssignMode mode = (request.getRoleAssignMode() == null)
                ? RoleAssignMode.RANDOM
                : request.getRoleAssignMode();

        // Room 생성 시 내부 검증 로직에서도 BusinessException 발생됨
        Room room = Room.createBuilder()
                .hostId(hostUserId)
                .locationId(request.getLocationId())
                .title(request.getTitle())
                .maxPlayers(request.getMaxPlayers())
                .policeCount(request.getPoliceCount())
                .thiefCount(request.getThiefCount())
                .startTime(request.getStartTime())
                .playTime(request.getPlayTime())
                .roleAssignMode(mode)
                .build();

        room.addParticipant(hostUserId);
        roomRepository.save(room);

        return room.getId();
    }

    @Override
    public Page<RoomDetailResponse> listRooms(RoomListRequest request, Pageable pageable) {
        return roomRepository.findRooms(request.getLocationId(), request.getStatus(), pageable)
                .map(roomConverter::toDetailResponse);
    }

    @Override
    public RoomDetailResponse getRoom(Long roomId) {
        Room room = findRoomWithParticipantsById(roomId);
        Set<Long> userIds = room.getParticipants().stream()
                .map(RoomParticipant::getUserId)
                .collect(Collectors.toSet());

        Map<Long, String> nicknameMap = userRepository.findNicknameMap(userIds);

        return roomConverter.toDetailResponse(room, nicknameMap);
    }

    @Override
    @Transactional
    public JoinRoomResponse joinRoom(Long roomId, Long userId) {
        Room room = findRoomById(roomId);
        room.addParticipant(userId);
        return roomConverter.toJoinResponse(room, userId);
    }

    @Override
    @Transactional
    public void leaveRoom(Long roomId, Long userId) {
        Room room = findRoomById(roomId);
        if (room.isHost(userId)) {
            room.delete(userId);
        } else {
            room.removeParticipant(userId);
        }
    }

    @Override
    @Transactional
    public void changeRole(Long roomId, Long userId, ParticipantRole newRole) {
        Room room = findRoomById(roomId);
        room.changeParticipantRole(userId, newRole);
    }

    // =================================================================
    // Helper Methods
    // =================================================================

    /**
     * Room 엔티티만 조회 (Lazy)
     * 예외 발생 시 BusinessException(ROOM_NOT_FOUND) 던짐
     */
    private Room findRoomById(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(RoomErrorCode.ROOM_NOT_FOUND));
    }

    /**
     * Room + Participants 조회 (Fetch Join)
     * 예외 발생 시 BusinessException(ROOM_NOT_FOUND) 던짐
     */
    private Room findRoomWithParticipantsById(Long roomId) {
        return roomRepository.findByIdWithParticipants(roomId)
                .orElseThrow(() -> new BusinessException(RoomErrorCode.ROOM_NOT_FOUND));
    }

    /*
    private void validateLocationExists(Long locationId) {
        if (!locationRepository.existsById(locationId)) {
            throw new BusinessException(RoomErrorCode.LOCATION_NOT_FOUND);
        }
    }
    */
}
