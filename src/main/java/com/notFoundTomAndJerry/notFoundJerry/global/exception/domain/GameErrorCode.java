package com.notFoundTomAndJerry.notFoundJerry.global.exception.domain;

import com.notFoundTomAndJerry.notFoundJerry.global.exception.ApiCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Game 도메인 에러 코드
 * 
 * 사용 예시:
 * throw new BusinessException(GameErrorCode.GAME_NOT_FOUND, "게임을 찾을 수 없습니다: " + gameId);
 */
@AllArgsConstructor
@Getter
public enum GameErrorCode implements ApiCode {

    // ========== 조회/상태 관련 (404, 400) ==========
    
    GAME_NOT_FOUND(HttpStatus.NOT_FOUND.value(), 4040, "게임을 찾을 수 없습니다"),
    PLAYER_NOT_FOUND(HttpStatus.NOT_FOUND.value(), 4041, "플레이어를 찾을 수 없습니다"),
    GAME_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND.value(), 4042, "게임 결과가 아직 저장되지 않았습니다"),
    
    INVALID_GAME_STATE(HttpStatus.BAD_REQUEST.value(), 4001, "유효하지 않은 게임 상태입니다"),
    EMPTY_ROLE_ASSIGNMENT(HttpStatus.BAD_REQUEST.value(), 4002, "역할 배치 정보가 비어있습니다"),
    NO_PLAYERS_IN_GAME(HttpStatus.BAD_REQUEST.value(), 4003, "게임에 참여한 플레이어가 없습니다"),
    SELF_VOTE_NOT_ALLOWED(HttpStatus.BAD_REQUEST.value(), 4004, "자기 자신에게 투표할 수 없습니다"),
    VOTE_TARGET_REQUIRED(HttpStatus.BAD_REQUEST.value(), 4005, "투표 대상을 선택해주세요"),
    VOTER_NOT_IN_GAME(HttpStatus.BAD_REQUEST.value(), 4006, "해당 게임 참가자만 MVP 투표할 수 있습니다"),
    
    // ========== 중복/충돌 관련 (409) ==========
    
    GAME_ALREADY_RUNNING(HttpStatus.CONFLICT.value(), 4090, "이미 진행 중인 게임이 있습니다"),
    ROLE_ALREADY_ASSIGNED(HttpStatus.CONFLICT.value(), 4091, "이미 역할이 배치되었습니다"),
    DUPLICATE_VOTE(HttpStatus.CONFLICT.value(), 4092, "이미 투표하셨습니다");

    private final Integer httpStatus;
    private final Integer code;
    private final String message;
}
