package com.notFoundTomAndJerry.notFoundJerry.domain.room.entity;

import com.notFoundTomAndJerry.notFoundJerry.domain.room.entity.enums.ParticipantRole;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "room_participants",
        uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "user_id"}))
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class RoomParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    @ToString.Exclude
    private Room room;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20)
    private ParticipantRole role;

    @CreatedDate
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    public static RoomParticipant create(Room room, Long userId) {
        RoomParticipant participant = new RoomParticipant();
        participant.room = room;
        participant.userId = userId;
        return participant;

    }

    public void assignRole(ParticipantRole role) {
        this.role = role;
    }
}