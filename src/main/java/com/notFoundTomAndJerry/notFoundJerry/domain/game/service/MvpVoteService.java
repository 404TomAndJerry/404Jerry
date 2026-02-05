package com.notFoundTomAndJerry.notFoundJerry.domain.game.service;

import com.notFoundTomAndJerry.notFoundJerry.domain.game.converter.GameConverter;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.dto.request.MvpVoteRequest;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.dto.response.MvpResultResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.dto.response.MvpVoteResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.Game;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.GamePlayer;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.MvpVote;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.repository.GamePlayerRepository;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.repository.GameRepository;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.repository.MvpVoteRepository;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.entity.Room;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.repository.RoomRepository;
import com.notFoundTomAndJerry.notFoundJerry.domain.user.repository.UserRepository;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.BusinessException;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.domain.GameErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
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
    private final GamePlayerRepository gamePlayerRepository;
    private final GameRepository gameRepository;
    private final RoomRepository roomRepository;
    private final GamePlayerService gamePlayerService;
    private final GameConverter gameConverter;
    private final UserRepository userRepository;

    /**
     * MVP 투표 (한 게임당 참가자 1인 1회, 스킵 포함).
     * - 투표자는 해당 게임의 플레이어여야 함.
     * - 이미 투표한 경우 DUPLICATE_VOTE.
     * - 스킵 시 targetUserId 없이 1회 투표로 간주.
     */
    @Transactional
    public MvpVoteResponse voteMvp(Long gameId, Long voterId, MvpVoteRequest request) {
        // 해당 게임 참가자만 투표 가능
        if (gamePlayerRepository.findByGameIdAndUserId(gameId, voterId).isEmpty()) {
            throw new BusinessException(GameErrorCode.VOTER_NOT_IN_GAME);
        }
        // 이미 투표했는지 확인 (한 게임당 1회만)
        if (mvpVoteRepository.existsByGameIdAndVoterId(gameId, voterId)) {
            throw new BusinessException(GameErrorCode.DUPLICATE_VOTE, "게임 " + gameId + "에서 투표자 " + voterId + "가 이미 투표했습니다.");
        }

        // 스킵 처리 (스킵도 1회 투표로 인정)
        if (Boolean.TRUE.equals(request.getSkip())) {
            MvpVote mvpVote = MvpVote.builder()
                    .gameId(gameId)
                    .voterId(voterId)
                    .targetUserId(null)
                    .build();
            mvpVoteRepository.save(mvpVote);
            tryTransitionRoomToWaitingIfAllVoted(gameId);

            return gameConverter.toVoteResponse(false, true);
        }

        // 일반 투표 처리
        Long targetUserId = request.getTargetUserId();
        if (targetUserId == null) {
            throw new BusinessException(GameErrorCode.VOTE_TARGET_REQUIRED);
        }
        if (targetUserId.equals(voterId)) {
            throw new BusinessException(GameErrorCode.SELF_VOTE_NOT_ALLOWED, "투표자 " + voterId + "는 자기 자신에게 투표할 수 없습니다.");
        }
        // 투표 대상은 같은 게임 참가자여야 함
        if (gamePlayerRepository.findByGameIdAndUserId(gameId, targetUserId).isEmpty()) {
            throw new BusinessException(GameErrorCode.VOTER_NOT_IN_GAME, "투표 대상은 해당 게임 참가자여야 합니다.");
        }

        MvpVote mvpVote = MvpVote.builder()
                .gameId(gameId)
                .voterId(voterId)
                .targetUserId(targetUserId)
                .build();
        mvpVoteRepository.save(mvpVote);
        tryTransitionRoomToWaitingIfAllVoted(gameId);

        return gameConverter.toVoteResponse(true, false);
    }

    // 해당 게임의 모든 플레이어가 MVP 투표를 완료했으면 Room 상태를 WAITING으로 전환하고 MVP 확정.
    private void tryTransitionRoomToWaitingIfAllVoted(Long gameId) {
        long voteCount = mvpVoteRepository.countByGameId(gameId);
        long playerCount = gamePlayerRepository.countByGameId(gameId);
        if (voteCount != playerCount) {
            return;
        }
        // MVP 확정
        finalizeMvp(gameId);
        // Room → WAITING (다음 게임 대기)
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new BusinessException(GameErrorCode.GAME_NOT_FOUND, "게임을 찾을 수 없습니다: " + gameId));
        Room room = roomRepository.findById(game.getRoomId())
                .orElse(null);
        if (room != null) {
            room.transitionToWaiting();
            roomRepository.save(room);
        }
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

        List<Long> userIds = mvpPlayers.stream()
                .map(GamePlayer::getUserId)
                .collect(Collectors.toList());
        Map<Long, String> nicknameMap = userRepository.findNicknameMap(userIds);

        return gameConverter.toMvpResultResponse(mvpPlayers, nicknameMap);
    }
}
