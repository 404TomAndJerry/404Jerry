package com.notFoundTomAndJerry.notFoundJerry.domain.game.repository;

import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.GamePlayer;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.enums.PlayerRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * GamePlayer 엔티티에 대한 Repository
 */
@Repository
public interface GamePlayerRepository extends JpaRepository<GamePlayer, Long> {

    // 게임 ID로 모든 플레이어 조회
    List<GamePlayer> findByGameId(Long gameId);

    // 게임 ID와 사용자 ID로 플레이어 조회
    Optional<GamePlayer> findByGameIdAndUserId(Long gameId, Long userId);

    // 게임의 MVP 플레이어들 조회
    List<GamePlayer> findByGameIdAndIsMvpTrue(Long gameId);

    // 게임의 특정 역할 플레이어 수 카운트
    long countByGameIdAndRole(Long gameId, PlayerRole role);

    // 게임의 플레이어 수 카운트
    long countByGameId(Long gameId);
}
