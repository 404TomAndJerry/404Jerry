package com.notFoundTomAndJerry.notFoundJerry.domain.game.service;

import com.notFoundTomAndJerry.notFoundJerry.domain.chat.service.ChatService;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.converter.GameConverter;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.dto.request.GameEndRequest;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.dto.request.GameStartRequest;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.dto.response.GameEndResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.dto.response.GameStartResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.dto.response.GameStatusResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.Game;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.GamePlayer;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.enums.EndReason;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.enums.GameStatus;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.enums.PlayerRole;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.enums.RoleAssignType;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.repository.GamePlayerRepository;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.repository.GameRepository;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.repository.RunawayLogRepository;
import com.notFoundTomAndJerry.notFoundJerry.domain.location.entity.Location;
import com.notFoundTomAndJerry.notFoundJerry.domain.location.repository.LocationRepository;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.entity.Room;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.entity.RoomParticipant;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.repository.RoomRepository;
import com.notFoundTomAndJerry.notFoundJerry.domain.stat.facade.GameStatFacade;
import com.notFoundTomAndJerry.notFoundJerry.domain.user.entity.User;
import com.notFoundTomAndJerry.notFoundJerry.domain.user.repository.UserRepository;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.BusinessException;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.domain.GameErrorCode;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.domain.RoomErrorCode;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.domain.StatErrorCode;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.domain.UserErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 게임 생명주기를 관리하는 서비스
 * - 게임 생성, 시작, 종료
 * - 게임 상태 조회
 *
 *  TODO : @Room Repository 참조하는 것
 *  - 추후 Service를 참조하는 것으로 변경
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameService {

  private final GameRepository gameRepository;
  private final GamePlayerRepository gamePlayerRepository;
  private final GameResultService gameResultService;
  private final GamePlayerService gamePlayerService;
  private final GameConverter gameConverter;
  private final RoomRepository roomRepository;
  private final UserRepository userRepository; // 유저 아이디 확인
  private final GameStatFacade gameStatFacade; // 통계 조회용 문지기
  private final RunawayLogRepository runawayLogRepository; // 탈주 기록 확인용
  private final LocationRepository locationRepository; // 지역 정보 조회
  private final ChatService chatService; // ChatService 주입

  // 새로운 게임 생성, roomId 방 ID, 생성된 게임
  @Transactional
  public Game createGame(Long roomId) {
    // 이미 진행 중인 게임이 있는지 확인
    if (isGameRunning(roomId)) {
      Game running = gameRepository.findByRoomIdAndStatus(roomId, GameStatus.RUNNING)
          .orElseThrow();
      throw new BusinessException(GameErrorCode.GAME_ALREADY_RUNNING,
          "방 " + roomId + "에 이미 진행 중인 게임이 있습니다. (게임 ID: " + running.getId() + ")");
    }
    Game game = Game.builder()
        .roomId(roomId)
        .build();
    return gameRepository.save(game);
  }

  // 게임 시작 (통합), request 게임 시작 요청
  @Transactional
  public GameStartResponse startGame(GameStartRequest request) {
    Long roomId = request.getRoomId();

    // Room 정보 먼저 조회 (없으면 게임 생성하지 않음 → orphan 방지)
    Room room = roomRepository.findByIdWithParticipants(roomId)
        .orElseThrow(() -> new BusinessException(RoomErrorCode.ROOM_NOT_FOUND));

    Game game = createGame(roomId);

    // Room 상태를 RUNNING으로 변경
    room.transitionToRunning();
    roomRepository.save(room);

    // 역할 배치
    RoleAssignType roleAssignment = request.getRoleAssignment();
    if (roleAssignment == RoleAssignType.RANDOM) {
      // Room의 참가자 목록에서 userId 추출
      List<Long> userIds = room.getParticipants().stream()
          .map(RoomParticipant::getUserId)
          .toList();

      gamePlayerService.assignRolesRandomly(game.getId(), userIds);
    } else if (roleAssignment == RoleAssignType.MANUAL) {
      // Room의 참가자 역할 정보를 Game에 복사
      gamePlayerService.assignRolesFromRoom(game.getId(), room.getParticipants());
    }

    // 게임 시작
    game.start();

    // 경찰/도둑 인원 수 조회
    long policeCount = gamePlayerRepository.countByGameIdAndRole(game.getId(), PlayerRole.POLICE);
    long thiefCount = gamePlayerRepository.countByGameIdAndRole(game.getId(), PlayerRole.THIEF);

    // Response 생성 (Converter 사용)
    return gameConverter.toStartResponse(game, (int) policeCount, (int) thiefCount);
  }

  // 게임 종료, gameId 게임 ID, request 게임 종료 정보 (종료 사유)
  @Transactional
  public GameEndResponse finishGame(Long gameId, GameEndRequest request) {
    // 게임 정보 조회
    Game game = gameRepository.findById(gameId)
        .orElseThrow(() -> new BusinessException(GameErrorCode.GAME_NOT_FOUND, "게임을 찾을 수 없습니다: " + gameId));

    // Room 정보 조회
    Room room = roomRepository.findByIdWithParticipants(game.getRoomId())
        .orElseThrow(() -> new BusinessException(RoomErrorCode.ROOM_NOT_FOUND, "방 " + game.getRoomId() + "을 찾을 수 없습니다."));

    // LocationRepository를 통해 Room의 locationId에 해당하는 regionName 추출
    Location location = locationRepository.findById(room.getLocationId())
        .orElseThrow(() -> new BusinessException(StatErrorCode.LOCATION_NOT_FOUND, "위치 정보를 찾을 수 없습니다."));
    String regionName = location.getRegionName();

    // 종료 사유에 따라 승리 팀 자동 결정
    EndReason endReason = request.getEndReason();
    PlayerRole winnerTeam = endReason.getWinner();

    // 게임 상태를 FINISHED로 변경 및 종료 사유 저장
    game.finish(endReason.getDescription());

    // Room 상태를 FINISHED로 변경 (MVP 투표가 모두 끝나면 MvpVoteService에서 WAITING으로 전환)
    room.transitionToFinished();
    roomRepository.save(room);

    // 게임 결과를 DB에 저장
    gameResultService.saveGameResults(
        gameId,
        winnerTeam,
        endReason.getDescription()
    );

    // 게임에 참여한 플레이어 목록을 가져와서 한 명씩 통계 업데이트
    List<GamePlayer> players = gamePlayerRepository.findByGameId(gameId);
    for (GamePlayer player : players) {
      // 유저의 나이 정보를 가져오기 위해 조회
      User user = userRepository.findById(player.getUserId())
          .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + player.getUserId()));

      // 탈주 여부 확인
      boolean isRunaway = runawayLogRepository.existsByGameIdAndUserId(gameId, player.getUserId());

      gameStatFacade.processGameStat(
          player.getUserId(),
          player.getRole(),
          player.getRole() == winnerTeam, // 승리 여부 계산
          isRunaway,                          // 탈주 여부 (필요 시 로직 추가)
          regionName,                       // 지역 정보 (room에서 가져오거나 기본값)
          user.getAge()
      );
    }

    // 게임이 끝났으면, 채팅 내역을 초기화 한다.
    chatService.resetChatRoom(game.getRoomId());

    // Response 생성 (Converter 사용)
    return gameConverter.toEndResponse(game);
  }

  // 게임 상태 조회, gameId 게임 ID, 게임 상태 정보
  public GameStatusResponse getGameStatus(Long gameId) {
    Game game = gameRepository.findById(gameId)
        .orElseThrow(() -> new BusinessException(GameErrorCode.GAME_NOT_FOUND, "게임을 찾을 수 없습니다: " + gameId));
    return gameConverter.toStatusResponse(game);
  }

  // 방에 진행 중인 게임이 있는지 확인, roomId 방 ID, 진행 중인 게임 존재 여부
  public boolean isGameRunning(Long roomId) {
    return gameRepository.existsByRoomIdAndStatus(roomId, GameStatus.RUNNING);
  }

  /** 방 ID로 최신 게임 1건 조회 */
  public Game findByRoomId(Long roomId) {
    return gameRepository.findTopByRoomIdOrderByCreatedAtDesc(roomId)
        .orElseThrow(() -> new BusinessException(GameErrorCode.GAME_NOT_FOUND, "해당 방의 게임을 찾을 수 없습니다: " + roomId));
  }
}