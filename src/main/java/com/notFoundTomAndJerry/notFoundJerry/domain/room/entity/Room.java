package com.notFoundTomAndJerry.notFoundJerry.domain.room.entity;

import com.notFoundTomAndJerry.notFoundJerry.domain.room.entity.enums.ParticipantRole;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.entity.enums.RoleAssignMode;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.entity.enums.RoomStatus;
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
@Table(name = "rooms")
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
            throw new IllegalArgumentException("시작 시간은 현재 이후여야 합니다.");
        }
        if (maxPlayers < 2) {
            throw new IllegalArgumentException("최소 인원은 2명 이상이어야 합니다.");
        }
        if (policeCount + thiefCount != maxPlayers) {
            throw new IllegalArgumentException("경찰 + 도둑 수는 정원과 일치해야 합니다.");
        }
    }

    private void validateCanJoin(Long userId) {
        if (this.status != RoomStatus.WAITING) {
            throw new IllegalStateException("WAITING 상태에서만 참가 가능합니다.");
        }
        if (participants.size() >= maxPlayers) {
            throw new IllegalStateException("정원이 가득 찼습니다.");
        }
        if (participants.stream().anyMatch(p -> p.getUserId().equals(userId))) {
            throw new IllegalStateException("이미 참가 중입니다.");
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
            throw new IllegalStateException("WAITING 상태에서만 역할을 변경할 수 있습니다.");
        }
        if (this.roleAssignMode != RoleAssignMode.MANUAL) {
            throw new IllegalStateException("MANUAL 모드에서만 역할을 선택할 수 있습니다.");
        }

        RoomParticipant participant = findParticipantByUserId(userId);

        // 역할 변경 가능 정책: 기존 역할을 고려해서 제한 체크
        int police = countRole(ParticipantRole.POLICE);
        int thief = countRole(ParticipantRole.THIEF);

        ParticipantRole oldRole = participant.getRole();
        if (oldRole == ParticipantRole.POLICE) police--;
        if (oldRole == ParticipantRole.THIEF) thief--;

        if (newRole == ParticipantRole.POLICE && police + 1 > policeCount) {
            throw new IllegalStateException("경찰 정원이 가득 찼습니다.");
        }
        if (newRole == ParticipantRole.THIEF && thief + 1 > thiefCount) {
            throw new IllegalStateException("도둑 정원이 가득 찼습니다.");
        }

        participant.assignRole(newRole);
    }

    public void delete(Long requesterId) {
        if (!isHost(requesterId)) {
            throw new IllegalStateException("방장만 삭제할 수 있습니다.");
        }
        if (this.status != RoomStatus.WAITING) {
            throw new IllegalStateException("WAITING 상태에서만 삭제 가능합니다.");
        }
        this.status = RoomStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }

    public void transitionToRunning() {
        if (this.status != RoomStatus.WAITING) {
            throw new IllegalStateException("WAITING 상태에서만 게임을 시작할 수 있습니다.");
        }
        this.status = RoomStatus.RUNNING;
    }


    private RoomParticipant findParticipantByUserId(Long userId) {
        return participants.stream()
                .filter(p -> p.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("참가 정보를 찾을 수 없습니다."));
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
