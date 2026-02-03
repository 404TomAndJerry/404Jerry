package com.notFoundTomAndJerry.notFoundJerry.domain.game.service;

import com.notFoundTomAndJerry.notFoundJerry.domain.game.converter.GameConverter;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.dto.request.MvpVoteRequest;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.dto.response.MvpResultResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.dto.response.MvpVoteResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.GamePlayer;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.MvpVote;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.repository.MvpVoteRepository;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.BusinessException;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.domain.GameErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MVP 투표 관리 서비스
 * - 투표 처리 및 집계
 * - MVP 확정
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MvpVoteService {

    private final MvpVoteRepository mvpVoteRepository;
    private final GamePlayerService gamePlayerService;
    private final GameConverter gameConverter;

    // MVP 투표, gameId 게임 ID, voterId 투표자 ID, request 투표 요청
    @Transactional
    public MvpVoteResponse voteMvp(Long gameId, Long voterId, MvpVoteRequest request) {
        // 이미 투표했는지 확인
        if (mvpVoteRepository.existsByGameIdAndVoterId(gameId, voterId)) {
            throw new BusinessException(GameErrorCode.DUPLICATE_VOTE, "게임 " + gameId + "에서 투표자 " + voterId + "가 이미 투표했습니다.");
        }

        // 스킵 처리
        if (Boolean.TRUE.equals(request.getSkip())) {
            MvpVote mvpVote = MvpVote.builder()
                    .gameId(gameId)
                    .voterId(voterId)
                    .targetUserId(null)
                    .build();
            mvpVoteRepository.save(mvpVote);

            return gameConverter.toVoteResponse(false, true);
        }

        // 일반 투표 처리
        Long targetUserId = request.getTargetUserId();
        if (targetUserId == null) {
            throw new BusinessException(GameErrorCode.VOTE_TARGET_REQUIRED);
        }
        // 자기 자신에게 투표 방지
        if (targetUserId.equals(voterId)) {
            throw new BusinessException(GameErrorCode.SELF_VOTE_NOT_ALLOWED, "투표자 " + voterId + "는 자기 자신에게 투표할 수 없습니다.");
        }

        MvpVote mvpVote = MvpVote.builder()
                .gameId(gameId)
                .voterId(voterId)
                .targetUserId(targetUserId)
                .build();
        mvpVoteRepository.save(mvpVote);

        return gameConverter.toVoteResponse(true, false);
    }

    // 투표 여부 확인, gameId 게임 ID, voterId 투표자 ID
    public boolean hasVoted(Long gameId, Long voterId) {
        return mvpVoteRepository.existsByGameIdAndVoterId(gameId, voterId);
    }

    // MVP 사용자 ID 조회 (동률 처리), gameId 게임 ID
    public List<Long> getMvpUserIds(Long gameId) {
        List<Object[]> voteCounts = mvpVoteRepository.findVoteCountsByGameId(gameId);

        if (voteCounts.isEmpty()) {
            return List.of();
        }

        // 최대 투표 수
        Long maxVotes = (Long) voteCounts.get(0)[1];

        // 최대 투표 수를 받은 모든 사용자 반환 (동률 포함)
        return voteCounts.stream()
                .filter(arr -> arr[1].equals(maxVotes))
                .map(arr -> (Long) arr[0])
                .collect(Collectors.toList());
    }

    // MVP 확정 (GamePlayer에 반영), gameId 게임 ID
    @Transactional
    public void finalizeMvp(Long gameId) {
        // 기존 MVP 전부 해제 후 새로 확정
        gamePlayerService.unsetAllMvp(gameId);

        List<Long> mvpUserIds = getMvpUserIds(gameId);

        if (mvpUserIds.isEmpty()) {
            // MVP가 없는 경우 (모두 스킵하거나 투표가 없음)
            return;
        }

        // GamePlayerService를 통해 MVP 설정
        gamePlayerService.setMvp(gameId, mvpUserIds);
    }

    // MVP 결과 조회 (등록 발생 시 투표 MVP 여부, 투표가 없을 경우 MVP 없음), gameId 게임 ID
    public MvpResultResponse getMvpResult(Long gameId) {
        List<GamePlayer> mvpPlayers = gamePlayerService.getMvpPlayers(gameId);
        return gameConverter.toMvpResultResponse(mvpPlayers);
    }
}
