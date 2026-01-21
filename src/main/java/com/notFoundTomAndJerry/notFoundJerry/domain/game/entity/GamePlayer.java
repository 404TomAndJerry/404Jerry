package com.notFoundTomAndJerry.notFoundJerry.domain.game.entity;

import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.enums.PlayerRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 게임 플레이어 엔티티
 * 게임에 참여한 최종 플레이어 스냅샷
 * - 게임 시작 시 room_participants를 기준으로 생성
 * - 역할 정보 고정
 * - MVP 여부 기록
 */

@Entity
@Table(name = "game_players")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class GamePlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_id", nullable = false)
    private Long gameId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private PlayerRole role;

    @Column(name = "is_mvp", nullable = false)
    private Boolean isMvp = false;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public GamePlayer(Long gameId, Long userId, PlayerRole role, Boolean isMvp) {
        this.gameId = gameId;
        this.userId = userId;
        this.role = role;
        this.isMvp = isMvp != null ? isMvp : false;
    }

    // MVP로 선정
    public void setAsMvp() {
        this.isMvp = true;
    }

    // MVP 선정 해제
    public void unsetMvp() {
        this.isMvp = false;
    }
}