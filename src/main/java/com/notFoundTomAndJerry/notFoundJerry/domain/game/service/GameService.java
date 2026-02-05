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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
    log.info("[게임 생성 시도] roomId: {}", roomId);
    if (isGameRunning(roomId)) {
      log.warn("[게임 생성 실패] 이미 진행 중인 게임이 존재함 - roomId: {}", roomId);
      Game running = gameRepository.findByRoomIdAndStatus(roomId, GameStatus.RUNNING).orElseThrow();
      throw new BusinessException(GameErrorCode.GAME_ALREADY_RUNNING);
    }
    Game game = Game.builder().roomId(roomId).build();
    Game savedGame = gameRepository.save(game);
    log.info("[게임 생성 완료] gameId: {}, roomId: {}", savedGame.getId(), roomId);
    return savedGame;
  }
  @Transactional
  public GameStartResponse startGame(GameStartRequest request) {
    Long roomId = request.getRoomId();
    log.info("[게임 시작 프로세스] 시작 - roomId: {}, 배정방식: {}", roomId, request.getRoleAssignment());

    Room room = roomRepository.findByIdWithParticipants(roomId)
        .orElseThrow(() -> new BusinessException(RoomErrorCode.ROOM_NOT_FOUND));

    Game game = createGame(roomId);

    // Room 상태 변경 로그
    log.info("[방 상태 전환] WAITING -> RUNNING - roomId: {}", roomId);
    room.transitionToRunning();
    roomRepository.save(room);

    // 역할 배정 로그
    if (request.getRoleAssignment() == RoleAssignType.RANDOM) {
      List<Long> userIds = room.getParticipants().stream().map(RoomParticipant::getUserId).toList();
      log.info("[역할 배정] 랜덤 배정 시작 - 대상 인원: {}명", userIds.size());
      gamePlayerService.assignRolesRandomly(game.getId(), userIds);
    } else {
      log.info("[역할 배정] 수동(Room 설정 복사) 배정 시작");
      gamePlayerService.assignRolesFromRoom(game.getId(), room.getParticipants());
    }

    game.start();
    long policeCount = gamePlayerRepository.countByGameIdAndRole(game.getId(), PlayerRole.POLICE);
    long thiefCount = gamePlayerRepository.countByGameIdAndRole(game.getId(), PlayerRole.THIEF);

    log.info("[게임 시작 완료] gameId: {}, 경찰: {}, 도둑: {}", game.getId(), policeCount, thiefCount);
    return gameConverter.toStartResponse(game, (int) policeCount, (int) thiefCount);
  }

  @Transactional
  public GameEndResponse finishGame(Long gameId, GameEndRequest request) {
    log.info("[게임 종료 프로세스] 시작 - gameId: {}, 사유: {}", gameId, request.getEndReason());

    Game game = gameRepository.findById(gameId)
        .orElseThrow(() -> new BusinessException(GameErrorCode.GAME_NOT_FOUND));

    Room room = roomRepository.findByIdWithParticipants(game.getRoomId())
        .orElseThrow(() -> new BusinessException(RoomErrorCode.ROOM_NOT_FOUND));

    Location location = locationRepository.findById(room.getLocationId())
        .orElseThrow(() -> new BusinessException(StatErrorCode.LOCATION_NOT_FOUND));

    String regionName = location.getRegionName();
    PlayerRole winnerTeam = request.getEndReason().getWinner();

    log.info("[게임 상태 변경] RUNNING -> FINISHED - gameId: {}, 승리팀: {}", gameId, winnerTeam);
    game.finish(request.getEndReason().getDescription());
    gameRepository.save(game);

    log.info("[방 상태 변경] RUNNING -> FINISHED - roomId: {}", room.getId());
    room.transitionToFinished();
    roomRepository.save(room);

    // 결과 저장 로그
    gameResultService.saveGameResults(gameId, winnerTeam, request.getEndReason().getDescription());
    log.info("[결과 저장 완료] gameId: {}", gameId);

    // 통계 업데이트 로그
    List<GamePlayer> players = gamePlayerRepository.findByGameId(gameId);
    log.info("[통계 업데이트] 시작 - 대상 플레이어 수: {}명", players.size());
    for (GamePlayer player : players) {
      User user = userRepository.findById(player.getUserId()).orElseThrow();
      boolean isRunaway = runawayLogRepository.existsByGameIdAndUserId(gameId, player.getUserId());

      gameStatFacade.processGameStat(
          player.getUserId(), player.getRole(), player.getRole() == winnerTeam,
          isRunaway, regionName, user.getAge()
      );
    }
    log.info("[통계 업데이트] 완료");
    // 채팅 초기화 로그
    log.info("[채팅 초기화] roomId: {}", game.getRoomId());
    chatService.resetChatRoom(game.getRoomId());

    return gameConverter.toEndResponse(game);
  }

  public GameStatusResponse getGameStatus(Long gameId) {
    log.info("[게임 상태 조회] gameId: {}", gameId);
    Game game = gameRepository.findById(gameId).orElseThrow();
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