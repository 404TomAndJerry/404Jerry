package com.notFoundTomAndJerry.notFoundJerry.domain.game.repository;

import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.Game;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.enums.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Game 엔티티에 대한 Repository
 */
@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    // 방 ID로 게임 조회
    Optional<Game> findByRoomId(Long roomId);

    // 방 ID와 상태로 게임 조회
    Optional<Game> findByRoomIdAndStatus(Long roomId, GameStatus status);

    // 방 ID에 진행 중인 게임이 있는지 확인
    boolean existsByRoomIdAndStatus(Long roomId, GameStatus status);
}
