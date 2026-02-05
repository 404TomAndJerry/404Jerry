package com.notFoundTomAndJerry.notFoundJerry.domain.game.service;

import com.notFoundTomAndJerry.notFoundJerry.domain.game.converter.GameConverter;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.dto.request.RoleAssignRequest;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.dto.response.PlayerRoleResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.GamePlayer;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.enums.PlayerRole;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.repository.GamePlayerRepository;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.entity.RoomParticipant;
import com.notFoundTomAndJerry.notFoundJerry.domain.user.repository.UserRepository;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.BusinessException;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.domain.GameErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 게임 플레이어 관리 서비스
 * - 역할 배치 및 조회
 * - MVP 설정 및 조회
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GamePlayerService {

    private final GamePlayerRepository gamePlayerRepository;
    private final GameConverter gameConverter;
    private final UserRepository userRepository;

    // ========== 역할 배치 ==========

    // 플레이어 역할 배치 (MANUAL), gameId 게임 ID, request 역할 배치 요청
    @Transactional
    public void assignRoles(Long gameId, RoleAssignRequest request) {
        long existingCount = gamePlayerRepository.countByGameId(gameId);
        if (existingCount > 0) {
            throw new BusinessException(GameErrorCode.ROLE_ALREADY_ASSIGNED, "게임 " + gameId + "에 이미 역할이 배치되었습니다.");
        }

        Map<Long, PlayerRole> playerRoles = request.getPlayerRoles();
        if (playerRoles == null || playerRoles.isEmpty()) {
            throw new BusinessException(GameErrorCode.EMPTY_ROLE_ASSIGNMENT);
        }

        for (Map.Entry<Long, PlayerRole> entry : playerRoles.entrySet()) {
            Long userId = entry.getKey();
            PlayerRole role = entry.getValue();
            GamePlayer gamePlayer = GamePlayer.builder()
                    .gameId(gameId)
                    .userId(userId)
                    .role(role)
                    .build();
            gamePlayerRepository.save(gamePlayer);
        }
    }

    // ========== 역할 조회 ==========

    // 개별 플레이어 역할 조회, gameId 게임 ID, userId 사용자 ID
    public PlayerRoleResponse getPlayerRole(Long gameId, Long userId) {
        GamePlayer player = gamePlayerRepository.findByGameIdAndUserId(gameId, userId)
                .orElseThrow(() -> new BusinessException(GameErrorCode.PLAYER_NOT_FOUND, "게임 " + gameId + "에서 플레이어 " + userId + "를 찾을 수 없습니다."));

        Map<Long, String> nicknameMap = userRepository.findNicknameMap(List.of(userId));
        String nickname = nicknameMap.getOrDefault(userId, "Unknown");

        return gameConverter.toRoleResponse(player, nickname);
    }

    // 전체 플레이어 역할 조회, gameId 게임 ID
    public List<PlayerRoleResponse> getAllPlayers(Long gameId) {
        List<GamePlayer> players = gamePlayerRepository.findByGameId(gameId);

        List<Long> userIds = players.stream()
                .map(GamePlayer::getUserId)
                .collect(Collectors.toList());
        Map<Long, String> nicknameMap = userRepository.findNicknameMap(userIds);

        return gameConverter.toRoleResponseList(players, nicknameMap);
    }

    // ========== MVP 관리 ==========

    // MVP 설정, gameId 게임 ID, mvpUserIds MVP로 선정된 사용자 ID 목록
    @Transactional
    public void setMvp(Long gameId, List<Long> mvpUserIds) {
        for (Long userId : mvpUserIds) {
            GamePlayer player = gamePlayerRepository.findByGameIdAndUserId(gameId, userId)
                    .orElseThrow(() -> new BusinessException(GameErrorCode.PLAYER_NOT_FOUND, "게임 " + gameId + "에서 플레이어 " + userId + "를 찾을 수 없습니다."));
            player.setAsMvp();
        }
    }

    // MVP 플레이어 조회, gameId 게임 ID
    public List<GamePlayer> getMvpPlayers(Long gameId) {
        log.info("getMvpPlayers 부분임.");
        return gamePlayerRepository.findByGameIdAndIsMvpTrue(gameId);
    }

    // 해당 게임의 모든 플레이어 MVP 해제 (MVP 재확정 시 사용)
    @Transactional
    public void unsetAllMvp(Long gameId) {
        List<GamePlayer> players = gamePlayerRepository.findByGameId(gameId);
        for (GamePlayer player : players) {
            if (player.getIsMvp()) {
                player.unsetMvp();
            }
        }
    }

    // 플레이어 역할 자동 배치 (RANDOM), gameId 게임 ID, userIds 참여 플레이어 ID 목록
    @Transactional
    public void assignRolesRandomly(Long gameId, List<Long> userIds) {
        // 이미 역할이 배치되었는지 확인
        long existingCount = gamePlayerRepository.countByGameId(gameId);
        if (existingCount > 0) {
            throw new BusinessException(GameErrorCode.ROLE_ALREADY_ASSIGNED, "게임 " + gameId + "에 이미 역할이 배치되었습니다.");
        }

        int totalPlayers = userIds.size();
        
        // 인원별 경찰/도둑 비율 결정 (Converter 사용)
        int policeCount = gameConverter.calculatePoliceCount(totalPlayers);
        int thiefCount = gameConverter.calculateThiefCount(totalPlayers);
        if (policeCount + thiefCount != totalPlayers) {
            throw new BusinessException(GameErrorCode.INVALID_GAME_STATE, "역할 배치 인원 수가 일치하지 않습니다.");
        }
        
        // 플레이어 목록 랜덤 섞기
        List<Long> shuffledUsers = new ArrayList<>(userIds);
        Collections.shuffle(shuffledUsers);
        
        // 역할 배치 (앞부터 경찰, 나머지 도둑)
        for (int i = 0; i < totalPlayers; i++) {
            PlayerRole role = i < policeCount ? PlayerRole.POLICE : PlayerRole.THIEF;
            GamePlayer gamePlayer = GamePlayer.builder()
                    .gameId(gameId)
                    .userId(shuffledUsers.get(i))
                    .role(role)
                    .build();
            gamePlayerRepository.save(gamePlayer);
        }
    }

    // 플레이어 역할 수동 배치 (MANUAL) - Room의 역할 정보 복사, gameId 게임 ID, participants Room 참가자 목록
    @Transactional
    public void assignRolesFromRoom(Long gameId, List<RoomParticipant> participants) {
        // 이미 역할이 배치되었는지 확인
        long existingCount = gamePlayerRepository.countByGameId(gameId);
        if (existingCount > 0) {
            throw new BusinessException(GameErrorCode.ROLE_ALREADY_ASSIGNED, "게임 " + gameId + "에 이미 역할이 배치되었습니다.");
        }

        // Room의 참가자 역할을 그대로 복사
        for (RoomParticipant participant : participants) {
            if (participant.getRole() == null) {
                throw new BusinessException(GameErrorCode.EMPTY_ROLE_ASSIGNMENT);
            }

            // ParticipantRole → PlayerRole 변환 (Enum 이름이 같아서 .name() 사용)
            PlayerRole playerRole = PlayerRole.valueOf(participant.getRole().name());

            GamePlayer gamePlayer = GamePlayer.builder()
                    .gameId(gameId)
                    .userId(participant.getUserId())
                    .role(playerRole)
                    .build();
            gamePlayerRepository.save(gamePlayer);
        }
    }
}
