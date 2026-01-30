package com.notFoundTomAndJerry.notFoundJerry.domain.game.entity;

import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.enums.GameStatus;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.BusinessException;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.domain.GameErrorCode;
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
 * 게임 엔티티 방에서 실제로 시작된 게임 인스턴스를 나타냄
 */
@Entity
@Table(name = "games")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GameStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "end_reason")
    private String endReason;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public Game(Long roomId, GameStatus status, LocalDateTime startedAt, String endReason) {
        this.roomId = roomId;
        this.status = status != null ? status : GameStatus.WAITING;
        this.startedAt = startedAt;
        this.endReason = endReason;
    }

    // 게임 시작
    public void start() {
        if (isRunning()) {
            throw new BusinessException(GameErrorCode.INVALID_GAME_STATE, "이미 진행 중인 게임입니다.");
        }
        if (isFinished()) {
            throw new BusinessException(GameErrorCode.INVALID_GAME_STATE, "종료된 게임은 다시 시작할 수 없습니다.");
        }
        this.status = GameStatus.RUNNING;
        this.startedAt = LocalDateTime.now();
    }

    // 게임 종료
    public void finish(String endReason) {
        if (!isRunning()) {
            throw new BusinessException(GameErrorCode.INVALID_GAME_STATE, "진행 중인 게임만 종료할 수 있습니다.");
        }
        this.status = GameStatus.FINISHED;
        this.endedAt = LocalDateTime.now();
        this.endReason = endReason;
    }

    // 게임이 진행 중인지 확인
    public boolean isRunning() {
        return this.status == GameStatus.RUNNING;
    }

    // 게임이 종료되었는지 확인
    public boolean isFinished() {
        return this.status == GameStatus.FINISHED;
    }
}
