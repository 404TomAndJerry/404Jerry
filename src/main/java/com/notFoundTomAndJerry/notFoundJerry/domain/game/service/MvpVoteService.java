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
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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

    @Transactional
    public MvpVoteResponse voteMvp(Long gameId, Long voterId, MvpVoteRequest request) {
        log.info("[투표 진행] gameId: {}, voterId: {}, skip: {}", gameId, voterId, request.getSkip());

        // 해당 게임 참가자만 투표 가능
        if (gamePlayerRepository.findByGameIdAndUserId(gameId, voterId).isEmpty()) {
            log.warn("[투표 실패] 참가자가 아님 - gameId: {}, voterId: {}", gameId, voterId);
            throw new BusinessException(GameErrorCode.VOTER_NOT_IN_GAME);
        }
        // 이미 투표했는지 확인
        if (mvpVoteRepository.existsByGameIdAndVoterId(gameId, voterId)) {
            log.warn("[투표 실패] 중복 투표 - gameId: {}, voterId: {}", gameId, voterId);
            throw new BusinessException(GameErrorCode.DUPLICATE_VOTE);
        }

        // 스킵 처리
        if (Boolean.TRUE.equals(request.getSkip())) {
            MvpVote mvpVote = MvpVote.builder()
                .gameId(gameId)
                .voterId(voterId)
                .targetUserId(null)
                .build();
            mvpVoteRepository.save(mvpVote);
            log.info("[투표 완료] 스킵 처리됨 - voterId: {}", voterId);

            tryTransitionRoomToWaitingIfAllVoted(gameId);
            return gameConverter.toVoteResponse(false, true);
        }

        // 일반 투표 처리
        Long targetUserId = request.getTargetUserId();
        log.info("[투표 진행] targetUserId: {}", targetUserId);

        if (targetUserId == null) {
            throw new BusinessException(GameErrorCode.VOTE_TARGET_REQUIRED);
        }
        if (targetUserId.equals(voterId)) {
            log.warn("[투표 실패] 본인 투표 불가 - voterId: {}", voterId);
            throw new BusinessException(GameErrorCode.SELF_VOTE_NOT_ALLOWED);
        }
        if (gamePlayerRepository.findByGameIdAndUserId(gameId, targetUserId).isEmpty()) {
            log.warn("[투표 실패] 대상이 참가자가 아님 - targetUserId: {}", targetUserId);
            throw new BusinessException(GameErrorCode.VOTER_NOT_IN_GAME);
        }

        MvpVote mvpVote = MvpVote.builder()
            .gameId(gameId)
            .voterId(voterId)
            .targetUserId(targetUserId)
            .build();
        mvpVoteRepository.save(mvpVote);
        log.info("[투표 완료] 투표 저장됨 - voterId: {} -> targetId: {}", voterId, targetUserId);

        tryTransitionRoomToWaitingIfAllVoted(gameId);
        return gameConverter.toVoteResponse(true, false);
    }

    private void tryTransitionRoomToWaitingIfAllVoted(Long gameId) {
        long voteCount = mvpVoteRepository.countByGameId(gameId);
        long playerCount = gamePlayerRepository.countByGameId(gameId);
        log.info("[투표 현황 확인] gameId: {}, 투표수: {}, 전체 인원: {}", gameId, voteCount, playerCount);

        if (voteCount != playerCount) {
            return;
        }

        log.info("[상태 전환 시작] 모든 인원 투표 완료. MVP 확정 및 방 상태 변경 진행.");
        finalizeMvp(gameId);

        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new BusinessException(GameErrorCode.GAME_NOT_FOUND));

        Room room = roomRepository.findById(game.getRoomId()).orElse(null);
        if (room != null) {
            room.transitionToWaiting();
            roomRepository.save(room);
            log.info("[상태 전환 완료] RoomId: {} -> WAITING", room.getId());
        }
    }

    public boolean hasVoted(Long gameId, Long voterId) {
        boolean voted = mvpVoteRepository.existsByGameIdAndVoterId(gameId, voterId);
        log.info("[투표 여부 조회] gameId: {}, voterId: {}, 결과: {}", gameId, voterId, voted);
        return voted;
    }

    public List<Long> getMvpUserIds(Long gameId) {
        List<Object[]> voteCounts = mvpVoteRepository.findVoteCountsByGameId(gameId);
        log.info("[MVP 집계 조회] gameId: {}, 집계 데이터 존재 여부: {}", gameId, !voteCounts.isEmpty());

        if (voteCounts.isEmpty()) {
            return List.of();
        }

        Long maxVotes = (Long) voteCounts.get(0)[1];
        log.info("[MVP 집계] 최대 득표수: {}", maxVotes);

        List<Long> mvpUserIds = voteCounts.stream()
            .filter(arr -> arr[1].equals(maxVotes))
            .map(arr -> (Long) arr[0])
            .collect(Collectors.toList());

        log.info("[MVP 집계 완료] MVP 선정 유저 IDs: {}", mvpUserIds);
        return mvpUserIds;
    }

    @Transactional
    public void finalizeMvp(Long gameId) {
        log.info("[MVP 확정 프로세스] 시작 - gameId: {}", gameId);
        gamePlayerService.unsetAllMvp(gameId);

        List<Long> mvpUserIds = getMvpUserIds(gameId);

        if (mvpUserIds.isEmpty()) {
            log.info("[MVP 확정 프로세스] 종료 - MVP 유저 없음 (모두 스킵 등)");
            return;
        }

        gamePlayerService.setMvp(gameId, mvpUserIds);
        log.info("[MVP 확정 프로세스] 완료 - 대상 IDs: {}", mvpUserIds);
    }

    public MvpResultResponse getMvpResult(Long gameId) {
        log.info("[MVP 결과 조회 시작] gameId: {}", gameId);

        // 새로 추가한 부분. [추가 로그] 현재 이 게임의 모든 플레이어와 그들의 MVP 여부를 생으로 찍어봄
        List<GamePlayer> allPlayers = gamePlayerRepository.findByGameId(gameId);
        allPlayers.forEach(p -> log.info("[데이터체크] userId: {}, isMvp: {}", p.getUserId(), p.getIsMvp()));

        List<GamePlayer> mvpPlayers = gamePlayerService.getMvpPlayers(gameId);
        log.info("[MVP 결과 조회 - 1] GamePlayer 목록: {}, size: {}", mvpPlayers, (mvpPlayers != null ? mvpPlayers.size() : 0));

        List<Long> userIds = mvpPlayers.stream()
            .map(GamePlayer::getUserId)
            .collect(Collectors.toList());
        log.info("[MVP 결과 조회 - 2] 추출된 userIds: {}", userIds);

        Map<Long, String> nicknameMap = userRepository.findNicknameMap(userIds);
        log.info("[MVP 결과 조회 - 3] 조회된 nicknameMap: {}", nicknameMap);

        MvpResultResponse response = gameConverter.toMvpResultResponse(mvpPlayers, nicknameMap);
        log.info("[MVP 결과 조회 완료] response 생성됨");

        return response;
    }
}