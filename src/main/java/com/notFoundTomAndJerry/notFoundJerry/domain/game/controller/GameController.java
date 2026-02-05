package com.notFoundTomAndJerry.notFoundJerry.domain.game.controller;

import com.notFoundTomAndJerry.notFoundJerry.domain.game.dto.request.*;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.dto.response.*;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.service.*;
import com.notFoundTomAndJerry.notFoundJerry.global.security.CustomPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * 게임 도메인 REST API Controller
 * - 게임 생명주기 관리
 * - 역할 배치 및 조회
 * - MVP 투표 및 결과
 * - 게임 결과 조회
 * - 탈주 처리
 */
@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
@Tag(name = "Game", description = "게임 관리 API")
public class GameController {

    private final GameService gameService;
    private final GamePlayerService gamePlayerService;
    private final MvpVoteService mvpVoteService;
    private final GameResultService gameResultService;
    private final RunawayService runawayService;

    // ========== 게임 생명주기 관리 ==========

    // 게임 시작: RANDOM=Room 참가자 랜덤 배치, MANUAL=Room에 배치된 참가자·역할을 그대로 가져와 배치
    @PostMapping("/start")
    @Operation(
        summary = "게임 시작",
        description = "방 ID와 역할 배치 방식을 받아 게임을 시작합니다. "
            + "RANDOM: Room 참가자 목록을 랜덤 배치. "
            + "MANUAL: Room 도메인에 배치된 참가자·역할을 조회해 그대로 Game에 반영합니다."
    )
    public ResponseEntity<GameStartResponse> startGame(
            @Valid @RequestBody GameStartRequest request
    ) {
        GameStartResponse response = gameService.startGame(request);
        return ResponseEntity.ok(response);
    }

    // 게임 종료, gameId 게임 ID, request 게임 종료 요청 (종료 사유)
    @PostMapping("/{gameId}/end")
    @Operation(summary = "게임 종료", description = "진행 중인 게임을 종료합니다.")
    public ResponseEntity<GameEndResponse> endGame(
            @Parameter(description = "게임 ID") @PathVariable Long gameId,
            @Valid @RequestBody GameEndRequest request
    ) {
        GameEndResponse response = gameService.finishGame(gameId, request);
        return ResponseEntity.ok(response);
    }

    // 게임 상태 조회, gameId 게임 ID
    @GetMapping("/{gameId}")
    @Operation(summary = "게임 상태 조회", description = "게임의 현재 상태를 조회합니다.")
    public ResponseEntity<GameStatusResponse> getGameStatus(
            @Parameter(description = "게임 ID") @PathVariable Long gameId
    ) {
        GameStatusResponse response = gameService.getGameStatus(gameId);
        return ResponseEntity.ok(response);
    }

    // 방 ID로 게임 조회 (해당 방의 게임 상태)
    @GetMapping("/by-room/{roomId}")
    @Operation(summary = "방 ID로 게임 조회", description = "방 ID로 해당 방의 게임을 조회합니다.")
    public ResponseEntity<GameStatusResponse> getGameByRoomId(
            @Parameter(description = "방 ID") @PathVariable Long roomId
    ) {
        GameStatusResponse response = gameService.getGameStatus(gameService.findByRoomId(roomId).getId());
        return ResponseEntity.ok(response);
    }

    // ========== 역할 관리 ==========

    // 역할 맵 직접 전송 (예외용). 수동 배치의 일반 흐름은 게임 시작 시 roleAssignment: MANUAL (Room 참가자 역할 자동 반영)
    @PostMapping("/{gameId}/roles")
    @Operation(
        summary = "역할 맵 직접 배치",
        description = "Request Body로 userId→역할 맵을 보내 역할을 배치합니다. "
            + "수동 배치는 보통 게임 시작 시 roleAssignment: MANUAL로 Room 참가자 역할을 자동 반영하므로, "
            + "이 API는 역할 맵을 직접 보낼 때만 사용합니다. 이미 역할이 배치된 게임이면 400."
    )
    public ResponseEntity<Void> assignRoles(
            @Parameter(description = "게임 ID") @PathVariable Long gameId,
            @Valid @RequestBody RoleAssignRequest request
    ) {
        gamePlayerService.assignRoles(gameId, request);
        return ResponseEntity.ok().build();
    }

    // 전체 플레이어 역할 조회, gameId 게임 ID
    @GetMapping("/{gameId}/roles")
    @Operation(
        summary = "전체 플레이어 역할 조회", 
        description = "게임에 참여한 모든 플레이어의 닉네임과 역할을 조회합니다. (User 도메인 연동)"
    )
    public ResponseEntity<List<PlayerRoleResponse>> getAllPlayerRoles(
            @Parameter(description = "게임 ID") @PathVariable Long gameId
    ) {
        List<PlayerRoleResponse> response = gamePlayerService.getAllPlayers(gameId);
        return ResponseEntity.ok(response);
    }

    // 개별 플레이어 역할 조회, gameId 게임 ID, userId JWT에서 자동 추출
    @GetMapping("/{gameId}/roles/me")
    @Operation(
        summary = "내 역할 조회",
        description = "JWT 토큰의 사용자 ID로 자신의 닉네임과 역할을 조회합니다. (User 도메인 연동)"
    )
    public ResponseEntity<PlayerRoleResponse> getMyRole(
            @Parameter(description = "게임 ID") @PathVariable Long gameId,
            @AuthenticationPrincipal CustomPrincipal principal
    ) {
        Long userId = resolveUserId(principal);
        PlayerRoleResponse response = gamePlayerService.getPlayerRole(gameId, userId);
        return ResponseEntity.ok(response);
    }

    // ========== MVP 투표 ==========

    // MVP 투표, gameId 게임 ID, request 투표 요청, userId JWT에서 자동 추출
    @PostMapping("/{gameId}/mvp/vote")
    @Operation(summary = "MVP 투표", description = "게임 종료 후 MVP를 투표합니다. skip=true로 투표를 건너뛸 수 있습니다. JWT 인증 필요.")
    public ResponseEntity<MvpVoteResponse> voteMvp(
            @Parameter(description = "게임 ID") @PathVariable Long gameId,
            @AuthenticationPrincipal CustomPrincipal principal,
            @Valid @RequestBody MvpVoteRequest request
    ) {
        Long userId = resolveUserId(principal);
        MvpVoteResponse response = mvpVoteService.voteMvp(gameId, userId, request);
        return ResponseEntity.ok(response);
    }

    // MVP 투표 여부 확인, gameId 게임 ID, userId JWT에서 자동 추출
    @GetMapping("/{gameId}/mvp/voted")
    @Operation(summary = "내 투표 여부 확인", description = "JWT 토큰의 사용자가 이미 투표했는지 확인합니다.")
    public ResponseEntity<Boolean> hasVoted(
            @Parameter(description = "게임 ID") @PathVariable Long gameId,
            @AuthenticationPrincipal CustomPrincipal principal
    ) {
        Long userId = resolveUserId(principal);
        boolean hasVoted = mvpVoteService.hasVoted(gameId, userId);
        return ResponseEntity.ok(hasVoted);
    }

    // MVP 결과 조회, gameId 게임 ID
    @GetMapping("/{gameId}/mvp")
    @Operation(summary = "MVP 결과 조회", description = "게임의 MVP 결과를 조회합니다. (동률 지원)")
    public ResponseEntity<MvpResultResponse> getMvpResult(
            @Parameter(description = "게임 ID") @PathVariable Long gameId
    ) {
        MvpResultResponse response = mvpVoteService.getMvpResult(gameId);
        return ResponseEntity.ok(response);
    }

    // ========== 게임 결과 ==========

    // 게임 결과 조회, gameId 게임 ID
    @GetMapping("/{gameId}/result")
    @Operation(summary = "게임 결과 조회", description = "게임 종료 후 최종 결과를 조회합니다. (승리 팀, MVP, 플레이어별 결과 포함)")
    public ResponseEntity<GameResultResponse> getGameResult(
            @Parameter(description = "게임 ID") @PathVariable Long gameId
    ) {
        GameResultResponse response = gameResultService.getGameResult(gameId);
        return ResponseEntity.ok(response);
    }

    // ========== 탈주 처리 ==========

    // 탈주 처리, gameId 게임 ID, userId JWT에서 자동 추출
    @PostMapping("/{gameId}/runaway")
    @Operation(summary = "탈주 처리", description = "게임 중 플레이어 탈주를 기록합니다. (본인만 가능) JWT 인증 필요.")
    public ResponseEntity<RunawayResponse> handleRunaway(
            @Parameter(description = "게임 ID") @PathVariable Long gameId,
            @AuthenticationPrincipal CustomPrincipal principal
    ) {
        Long userId = resolveUserId(principal);
        RunawayResponse response = runawayService.handleRunaway(gameId, userId);
        return ResponseEntity.ok(response);
    }

    //JWT/OAuth2 Principal에서 userId(Long) 추출, CustomPrincipal: OAuth2 로그인, String: JWT 토큰 (userId가 Subject로 저장됨)
    private Long resolveUserId(Object principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }
        if (principal instanceof CustomPrincipal) {
            return ((CustomPrincipal) principal).getUserId();
        }
        if (principal instanceof String) {
            return Long.parseLong((String) principal);
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 인증 정보입니다.");
    }
}
