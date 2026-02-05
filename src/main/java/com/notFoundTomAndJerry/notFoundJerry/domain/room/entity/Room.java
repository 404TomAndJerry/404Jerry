package com.notFoundTomAndJerry.notFoundJerry.domain.room.entity;

import com.notFoundTomAndJerry.notFoundJerry.domain.room.entity.enums.ParticipantRole;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.entity.enums.RoleAssignMode;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.entity.enums.RoomStatus;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.BusinessException;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.domain.RoomErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "rooms", indexes = {
    @Index(name = "idx_room_location_status", columnList = "location_id, status")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "host_id", nullable = false)
    private Long hostId;

    // 필요하면 locationId -> Point 로 바꿀 수 있음 (현재는 유지)
    @Column(name = "location_id", nullable = false)
    private Long locationId;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "max_players", nullable = false)
    private Integer maxPlayers;

    @Column(name = "police_count", nullable = false)
    private Integer policeCount;

    @Column(name = "thief_count", nullable = false)
    private Integer thiefCount;

    @Column(name = "start_time", nullable = true)
    private LocalDateTime startTime;

    @Column(name = "play_time", nullable = false)
    private Integer playTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RoomStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_assign_mode", nullable = false, length = 20)
    private RoleAssignMode roleAssignMode;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<RoomParticipant> participants;

    // 정적 팩토리 메서드
    @Builder(builderMethodName = "createBuilder")
    public static Room create(
        Long hostId,
        Long locationId,
        String title,
        Integer maxPlayers,
        Integer policeCount,
        Integer thiefCount,
        LocalDateTime startTime,
        Integer playTime,
        RoleAssignMode roleAssignMode
    ) {
        validateCreateParams(startTime, maxPlayers, policeCount, thiefCount);

        Room room = new Room();
        room.hostId = hostId;
        room.locationId = locationId;
        room.title = title;
        room.maxPlayers = maxPlayers;
        room.policeCount = policeCount;
        room.thiefCount = thiefCount;
        room.startTime = startTime;
        room.playTime = playTime;

        // 기본값 설정
        room.roleAssignMode = (roleAssignMode == null) ? RoleAssignMode.RANDOM : roleAssignMode;
        room.status = RoomStatus.WAITING;
        room.participants = new ArrayList<>();
        return room;
    }

    // 검증 로직
    private static void validateCreateParams(LocalDateTime startTime, Integer maxPlayers,
                                             Integer policeCount, Integer thiefCount) {
        if (startTime != null && startTime.isBefore(LocalDateTime.now())) {
            throw new BusinessException(RoomErrorCode.INVALID_START_TIME);
        }
        if (maxPlayers < 6) {
            throw new BusinessException(RoomErrorCode.INVALID_MIN_PLAYERS);
        }
        if (policeCount + thiefCount != maxPlayers) {
            throw new BusinessException(RoomErrorCode.INVALID_ROLE_COUNT);
        }
    }

    private void validateCanJoin(Long userId) {
        if (this.status != RoomStatus.WAITING) {
            throw new BusinessException(RoomErrorCode.INVALID_ROOM_STATUS); // 상태 관련 에러
        }
        if (participants.size() >= maxPlayers) {
            throw new BusinessException(RoomErrorCode.ROOM_FULL);
        }
        if (participants.stream().anyMatch(p -> p.getUserId().equals(userId))) {
            throw new BusinessException(RoomErrorCode.ALREADY_JOINED);
        }
    }


    // 비즈니스 로직
    public void addParticipant(Long userId) {
        validateCanJoin(userId);
        RoomParticipant participant = RoomParticipant.create(this, userId);
        participants.add(participant);
    }

    public void removeParticipant(Long userId) {
        RoomParticipant participant = findParticipantByUserId(userId);
        participants.remove(participant);
    }

    /**
     * MANUAL 모드에서만 허용: 참가자가 본인 역할 선택/변경 가능.
     * role==null은 "선택 안 함"이라서 여기서 거절하지 않음.
     */
    public void changeParticipantRole(Long userId, ParticipantRole newRole) {
        if (this.status != RoomStatus.WAITING) {
            throw new BusinessException(RoomErrorCode.INVALID_ROOM_STATUS);
        }
        if (this.roleAssignMode != RoleAssignMode.MANUAL) {
            throw new BusinessException(RoomErrorCode.ROLE_ASSIGN_NOT_ALLOWED);
        }

        RoomParticipant participant = findParticipantByUserId(userId);

        // 역할 변경 가능 정책: 기존 역할을 고려해서 제한 체크
        int police = countRole(ParticipantRole.POLICE);
        int thief = countRole(ParticipantRole.THIEF);

        ParticipantRole oldRole = participant.getRole();
        if (oldRole == ParticipantRole.POLICE) police--;
        if (oldRole == ParticipantRole.THIEF) thief--;

        if (newRole == ParticipantRole.POLICE && police + 1 > policeCount) {
            throw new BusinessException(RoomErrorCode.ROLE_COUNT_EXCEEDED);
        }
        if (newRole == ParticipantRole.THIEF && thief + 1 > thiefCount) {
            throw new BusinessException(RoomErrorCode.ROLE_COUNT_EXCEEDED);
        }

        participant.assignRole(newRole);
    }

    public void delete(Long requesterId) {
        if (!isHost(requesterId)) {
            throw new BusinessException(RoomErrorCode.ONLY_HOST_ALLOWED);
        }
        if (this.status != RoomStatus.WAITING) {
            throw new BusinessException(RoomErrorCode.INVALID_ROOM_STATUS);
        }
        this.status = RoomStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }

    public void transitionToRunning() {
        if (this.status != RoomStatus.WAITING) {
            throw new BusinessException(RoomErrorCode.INVALID_ROOM_STATUS);
        }
        this.status = RoomStatus.RUNNING;
    }

    public void transitionToFinished() {
        if (this.status != RoomStatus.RUNNING) {
            throw new BusinessException(RoomErrorCode.INVALID_ROOM_STATUS);
        }
        this.status = RoomStatus.FINISHED;
    }

    public void transitionToWatting() {
        if (this.status != RoomStatus.FINISHED) {
            throw new BusinessException(RoomErrorCode.INVALID_ROOM_STATUS);
        }
        this.status = RoomStatus.WAITING;
    }

    public void transitionToWaiting() {
        if (this.status != RoomStatus.FINISHED) {
            throw new BusinessException(RoomErrorCode.INVALID_ROOM_STATUS);
        }
        this.status = RoomStatus.WAITING;
    }

    private RoomParticipant findParticipantByUserId(Long userId) {
        return participants.stream()
                .filter(p -> p.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(RoomErrorCode.PARTICIPANT_NOT_FOUND));
    }

    private int countRole(ParticipantRole role) {
        return (int) participants.stream()
                .filter(p -> role.equals(p.getRole()))
                .count();
    }

    public boolean isHost(Long userId) {
        return this.hostId.equals(userId);
    }

    public List<RoomParticipant> getParticipants() {
        return Collections.unmodifiableList(participants);
    }

    public int getCurrentParticipantCount() {
        return participants.size();
    }
}
