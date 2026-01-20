package com.notFoundTomAndJerry.notFoundJerry.domain.game.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

// MVP 투표 엔티티 게임 종료 후 MVP 투표 기록
@Entity
@Table(name = "mvp_votes",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"game_id", "voter_id"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class MvpVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_id", nullable = false)
    private Long gameId;

    @Column(name = "voter_id", nullable = false)
    private Long voterId;

    @Column(name = "target_user_id")
    private Long targetUserId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public MvpVote(Long gameId, Long voterId, Long targetUserId) {
        this.gameId = gameId;
        this.voterId = voterId;
        this.targetUserId = targetUserId; // null일 수 있음 (스킵 투표)
    }
}