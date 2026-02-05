package com.notFoundTomAndJerry.notFoundJerry.domain.game.service;

import com.notFoundTomAndJerry.notFoundJerry.domain.game.converter.GameConverter;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.dto.response.GameResultResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.Game;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.GamePlayer;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.GameResult;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.enums.PlayerRole;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.repository.GamePlayerRepository;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.repository.GameRepository;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.repository.GameResultRepository;
import com.notFoundTomAndJerry.notFoundJerry.domain.user.repository.UserRepository;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.BusinessException;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.domain.GameErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 게임 결과 관리 서비스
 * - 게임 결과 저장 및 조회
 * - 사용자 통계
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameResultService {

    private final GameResultRepository gameResultRepository;
    private final GameRepository gameRepository;
    private final GamePlayerRepository gamePlayerRepository;
    private final GameConverter gameConverter;
    private final UserRepository userRepository;

    // 게임 결과 저장, gameId 게임 ID, winnerTeam 승리 팀, endReason 종료 사유
    @Transactional
    public void saveGameResults(Long gameId, PlayerRole winnerTeam, String endReason) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new BusinessException(GameErrorCode.GAME_NOT_FOUND, "게임을 찾을 수 없습니다: " + gameId));

        List<GamePlayer> players = gamePlayerRepository.findByGameId(gameId);
        if (players.isEmpty()) {
            throw new BusinessException(GameErrorCode.NO_PLAYERS_IN_GAME, "게임 " + gameId + "에 참여한 플레이어가 없습니다.");
        }

        // 각 플레이어별 결과 생성 (Batch Insert)
        List<GameResult> results = players.stream()
                .map(player -> GameResult.builder()
                        .gameId(gameId)
                        .userId(player.getUserId())
                        .role(player.getRole())
                        .isWinner(player.getRole() == winnerTeam)
                        .build())
                .collect(Collectors.toList());

        gameResultRepository.saveAll(results);
    }

    // 게임 결과 조회, gameId 게임 ID
    public GameResultResponse getGameResult(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new BusinessException(GameErrorCode.GAME_NOT_FOUND, "게임을 찾을 수 없습니다: " + gameId));

        // 게임 결과 조회
        List<GameResult> gameResults = gameResultRepository.findByGameId(gameId);
        if (gameResults.isEmpty()) {
            throw new BusinessException(GameErrorCode.GAME_RESULT_NOT_FOUND, "게임 " + gameId + "의 결과가 아직 저장되지 않았습니다.");
        }

        // 게임 플레이어 조회 (MVP 정보 포함)
        List<GamePlayer> gamePlayers = gamePlayerRepository.findByGameId(gameId);

        // MVP 플레이어 조회
        List<GamePlayer> mvpPlayers = gamePlayerRepository.findByGameIdAndIsMvpTrue(gameId);

        // User 닉네임 조회 (gameResults + gamePlayers의 userId 수집)
        List<Long> userIds = Stream.concat(
                        gameResults.stream().map(r -> r.getUserId()),
                        gamePlayers.stream().map(GamePlayer::getUserId)
                )
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> nicknameMap = userRepository.findNicknameMap(userIds);

        // Response 생성 (Converter 사용)
        return gameConverter.toGameResultResponse(game, gameResults, gamePlayers, mvpPlayers, nicknameMap);
    }
}
