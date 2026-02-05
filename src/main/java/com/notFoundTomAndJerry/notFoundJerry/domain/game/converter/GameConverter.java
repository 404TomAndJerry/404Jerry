package com.notFoundTomAndJerry.notFoundJerry.domain.game.converter;

import com.notFoundTomAndJerry.notFoundJerry.domain.game.dto.response.*;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.*;

import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.enums.PlayerRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Game 도메인 DTO 변환 통합 컨버터
 */
@Slf4j
@Component
public class GameConverter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    // ========== Game 관련 Response 변환 ==========

    // Game → GameStartResponse 변환 (게임 시작)
    public GameStartResponse toStartResponse(Game game, int policeCount, int thiefCount) {
        return GameStartResponse.builder()
                .gameId(game.getId())
                .status(game.getStatus().name())
                .startedAt(game.getStartedAt().format(FORMATTER))
                .policeCount(policeCount)
                .thiefCount(thiefCount)
                .build();
    }

    // Game → GameEndResponse 변환 (게임 종료)
    public GameEndResponse toEndResponse(Game game) {
        return GameEndResponse.builder()
                .gameId(game.getId())
                .status(game.getStatus().name())
                .endedAt(game.getEndedAt().format(FORMATTER))
                .build();
    }

    // Game → GameStatusResponse 변환 (게임 상태 조회)
    public GameStatusResponse toStatusResponse(Game game) {
        return GameStatusResponse.builder()
                .gameId(game.getId())
                .status(game.getStatus())
                .startedAt(game.getStartedAt())
                .endedAt(game.getEndedAt())
                .roomId(game.getRoomId())
                .build();
    }

    // ========== GamePlayer 관련 Response 변환 ==========

    // GamePlayer → PlayerRoleResponse 변환 (개별 플레이어 역할, nickname 지정)
    public PlayerRoleResponse toRoleResponse(GamePlayer player, String nickname) {
        return PlayerRoleResponse.builder()
                .userId(player.getUserId())
                .role(player.getRole())
                .nickname(nickname != null ? nickname : "Unknown")
                .build();
    }

    // GamePlayer 목록 → PlayerRoleResponse 목록 변환 (nicknameMap: userId → nickname)
    public List<PlayerRoleResponse> toRoleResponseList(List<GamePlayer> players, Map<Long, String> nicknameMap) {
        if (nicknameMap == null) {
            nicknameMap = Collections.emptyMap();
        }
        final Map<Long, String> map = nicknameMap;
        return players.stream()
                .map(p -> toRoleResponse(p, map.getOrDefault(p.getUserId(), "Unknown")))
                .collect(Collectors.toList());
    }

    // ========== MVP 관련 Response 변환 ==========

    // MVP 투표 결과 → MvpVoteResponse 변환
    public MvpVoteResponse toVoteResponse(boolean voted, boolean skipped) {
        String message = skipped ? "투표를 건너뛰었습니다." : "투표가 완료되었습니다.";
        return MvpVoteResponse.builder()
                .voted(voted)
                .skipped(skipped)
                .message(message)
                .build();
    }
    // MVP 플레이어 목록 → MvpResultResponse 변환 (nicknameMap: userId → nickname)
    public MvpResultResponse toMvpResultResponse(List<GamePlayer> mvpPlayers, Map<Long, String> nicknameMap) {
        log.info("[컨버터 시작] MvpResultResponse 변환 시도. mvpPlayers 수: {}", (mvpPlayers != null ? mvpPlayers.size() : 0));

        if (nicknameMap == null) {
            log.warn("[컨버터 경고] nicknameMap이 null로 넘어왔습니다. 빈 맵으로 대체합니다.");
            nicknameMap = Collections.emptyMap();
        }

        final Map<Long, String> map = nicknameMap;

        List<MvpResultResponse.MvpPlayerDto> mvpPlayerDtos = mvpPlayers.stream()
            .map(player -> {
                String nickname = map.getOrDefault(player.getUserId(), "User_" + player.getUserId());
                log.info("[컨버터 진행] 유저 변환 중 - userId: {}, nickname: {}", player.getUserId(), nickname);

                return MvpResultResponse.MvpPlayerDto.builder()
                    .userId(player.getUserId())
                    .nickname(nickname)
                    .build();
            })
            .collect(Collectors.toList());

        MvpResultResponse response = MvpResultResponse.builder()
            .mvpPlayers(mvpPlayerDtos)
            .build();

        log.info("[컨버터 완료] 최종 DTO 생성됨. 포함된 MVP 수: {}", mvpPlayerDtos.size());

        return response;
    }

    // ========== GameResult 관련 Response 변환 ==========

    // 게임 결과 전체 데이터 → GameResultResponse 변환 (nicknameMap: userId → nickname)
    public GameResultResponse toGameResultResponse(
            Game game,
            List<GameResult> gameResults,
            List<GamePlayer> gamePlayers,
            List<GamePlayer> mvpPlayers,
            Map<Long, String> nicknameMap) {

        if (nicknameMap == null) {
            nicknameMap = Collections.emptyMap();
        }
        final Map<Long, String> map = nicknameMap;

        // 승리 팀 결정 (첫 번째 승자의 역할로 판단)
        PlayerRole winnerTeam = gameResults.stream()
                .filter(GameResult::getIsWinner)
                .map(GameResult::getRole)
                .findFirst()
                .orElse(null);

        // MVP 닉네임 목록
        List<String> mvpUserNicknames = mvpPlayers.stream()
                .map(player -> map.getOrDefault(player.getUserId(), "User_" + player.getUserId()))
                .collect(Collectors.toList());

        // 플레이어별 결과 생성
        List<GameResultResponse.PlayerResultDto> playerResults = gameResults.stream()
                .map(result -> toPlayerResultDto(result, gamePlayers, map))
                .collect(Collectors.toList());

        return GameResultResponse.builder()
                .gameId(game.getId())
                .winnerTeam(winnerTeam)
                .endReason(game.getEndReason())
                .mvpUserNicknames(mvpUserNicknames)
                .playerResults(playerResults)
                .build();
    }

    // GameResult → PlayerResultDto 변환 (내부 사용)
    private GameResultResponse.PlayerResultDto toPlayerResultDto(
            GameResult result,
            List<GamePlayer> gamePlayers,
            Map<Long, String> nicknameMap) {

        // 해당 플레이어의 MVP 여부 확인
        boolean isMvp = gamePlayers.stream()
                .anyMatch(gp -> gp.getUserId().equals(result.getUserId()) && gp.getIsMvp());

        String nickname = nicknameMap.getOrDefault(result.getUserId(), "User_" + result.getUserId());

        return GameResultResponse.PlayerResultDto.builder()
                .userId(result.getUserId())
                .nickname(nickname)
                .role(result.getRole())
                .isWinner(result.getIsWinner())
                .isMvp(isMvp)
                .build();
    }

    // ========== Runaway 관련 Response 변환 ==========

    // 탈주 처리 → RunawayResponse 변환
    public RunawayResponse toRunawayResponse() {
        return RunawayResponse.builder()
                .runaway(true)
                .runawayAt(LocalDateTime.now().format(FORMATTER))
                .build();
    }

    // ========== Role 계산 로직 (경찰/도둑 비율) ==========

    // 경찰 인원 수 계산 (6~10명: 개별 비율, 11명 이상: 2:3 비율)
    // 최소인원 6명, 최대인원 30명으로 설정
    public int calculatePoliceCount(int totalPlayers) {
        if (totalPlayers < 6 || totalPlayers > 30) {
            throw new IllegalArgumentException("플레이어 수는 6명 이상 30명 이하여야 합니다.");
        }
        if (totalPlayers <= 10) {
            return switch (totalPlayers) {
                case 6 -> 2;
                case 7 -> 2;
                case 8 -> 3;
                case 9 -> 3;
                case 10 -> 4;
                default -> throw new IllegalArgumentException("유효하지 않은 플레이어 수: " + totalPlayers);
            };
        }

        // 11명 이상: 경찰 40% (2:3 비율)
        return (int) Math.round(totalPlayers * 0.4);
    }

    // 도둑 인원 수 계산 (전체 - 경찰)
    public int calculateThiefCount(int totalPlayers) {
        return totalPlayers - calculatePoliceCount(totalPlayers);
    }
}