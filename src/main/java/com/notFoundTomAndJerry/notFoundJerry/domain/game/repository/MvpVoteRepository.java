package com.notFoundTomAndJerry.notFoundJerry.domain.game.repository;

import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.MvpVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MvpVote 엔티티에 대한 Repository
 */
@Repository
public interface MvpVoteRepository extends JpaRepository<MvpVote, Long> {

    // 게임 ID로 모든 투표 조회
    List<MvpVote> findByGameId(Long gameId);

    // 게임 ID와 투표자 ID로 투표 조회
    Optional<MvpVote> findByGameIdAndVoterId(Long gameId, Long voterId);

    // 투표자가 이미 투표했는지 확인
    boolean existsByGameIdAndVoterId(Long gameId, Long voterId);

    // 게임별 투표 수 (전원 투표 완료 여부 확인용)
    long countByGameId(Long gameId);

    // 게임에서 특정 사용자가 받은 투표 수 카운트
    long countByGameIdAndTargetUserId(Long gameId, Long targetUserId);

    // 게임에서 사용자별 투표 수 조회 (동률 MVP 처리를 위해 Service에서 최대값 필터링)
    // 반환: Object[] { targetUserId(Long), voteCount(Long) }
    @Query("SELECT mv.targetUserId, COUNT(mv) " +
           "FROM MvpVote mv " +
           "WHERE mv.gameId = :gameId AND mv.targetUserId IS NOT NULL " +
           "GROUP BY mv.targetUserId " +
           "ORDER BY COUNT(mv) DESC")
    List<Object[]> findVoteCountsByGameId(@Param("gameId") Long gameId);
}
