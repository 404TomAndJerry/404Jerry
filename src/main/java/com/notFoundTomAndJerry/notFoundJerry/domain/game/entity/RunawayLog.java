package com.notFoundTomAndJerry.notFoundJerry.domain.game.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 탈주 로그 엔티티 게임 중 특정 사용자의 탈주 시점 기록
 * - 히스토리
 * - 통계용 데이터
 */
@Entity
@Table(name = "runaway_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class RunawayLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_id", nullable = false)
    private Long gameId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @CreatedDate
    @Column(name = "runaway_at", nullable = false, updatable = false)
    private LocalDateTime runawayAt;

    @Builder
    public RunawayLog(Long gameId, Long userId) {
        this.gameId = gameId;
        this.userId = userId;
    }
}