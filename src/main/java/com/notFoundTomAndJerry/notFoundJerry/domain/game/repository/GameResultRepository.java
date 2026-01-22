package com.notFoundTomAndJerry.notFoundJerry.domain.game.repository;

import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.GameResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * GameResult 엔티티에 대한 Repository
 */
@Repository
public interface GameResultRepository extends JpaRepository<GameResult, Long> {

    // 게임 ID로 모든 결과 조회
    List<GameResult> findByGameId(Long gameId);

    // 사용자 ID로 모든 게임 결과 조회
    List<GameResult> findByUserId(Long userId);

    // 사용자의 승리 횟수 카운트
    long countByUserIdAndIsWinnerTrue(Long userId);

    // 사용자의 총 게임 수 카운트
    long countByUserId(Long userId);
}
