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
 * 게임 결과 엔티티
 * 게임 종료 후 개인별 결과 기록
 * - 승/패 여부
 * - 역할별 결과 저장
 * - 게임 종료 후 결과 저장
 */
@Entity
@Table(name = "game_results")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class GameResult {

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

    @Column(name = "is_winner", nullable = false)
    private Boolean isWinner;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public GameResult(Long gameId, Long userId, PlayerRole role, Boolean isWinner) {
        this.gameId = gameId;
        this.userId = userId;
        this.role = role;
        this.isWinner = isWinner;
    }
}