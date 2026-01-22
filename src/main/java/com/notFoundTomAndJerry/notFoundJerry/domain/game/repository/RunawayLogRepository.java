package com.notFoundTomAndJerry.notFoundJerry.domain.game.repository;

import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.RunawayLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * RunawayLog 엔티티에 대한 Repository
 */
@Repository
public interface RunawayLogRepository extends JpaRepository<RunawayLog, Long> {

    // 게임 ID로 탈주 로그 조회
    List<RunawayLog> findByGameId(Long gameId);

    // 사용자 ID로 탈주 로그 조회
    List<RunawayLog> findByUserId(Long userId);

    // 사용자의 탈주 횟수 카운트
    long countByUserId(Long userId);

    // 게임에서 특정 사용자가 탈주했는지 확인
    boolean existsByGameIdAndUserId(Long gameId, Long userId);
}
