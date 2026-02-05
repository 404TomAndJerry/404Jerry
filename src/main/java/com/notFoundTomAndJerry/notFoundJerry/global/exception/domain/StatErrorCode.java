package com.notFoundTomAndJerry.notFoundJerry.global.exception.domain;

import com.notFoundTomAndJerry.notFoundJerry.global.exception.ApiCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
@Slf4j
public enum StatErrorCode implements ApiCode {
  // 조회 실패 (Not Found) - DB/Redis
  USER_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND.value(), 401, "해당 유저의 전적 정보를 찾을 수 없습니다."),
  RANKING_NOT_FOUND(HttpStatus.NOT_FOUND.value(), 402, "랭킹 정보를 조회할 수 없습니다."),

  // 유효성 검증 실패 (Validation) - 비즈니스 로직
  INVALID_PLAYER_ROLE(HttpStatus.BAD_REQUEST.value(), 401, "유효하지 않은 플레이어 역할입니다. (POLICE/THIEF)"),
  INVALID_AGE_VALUE(HttpStatus.BAD_REQUEST.value(), 402, "나이 정보가 올바르지 않습니다."),
  INVALID_REGION_NAME(HttpStatus.BAD_REQUEST.value(), 403, "지역 이름이 비어있거나 올바르지 않습니다."),

  // 외부 시스템 연동 실패 (Redis)
  REDIS_CONNECTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), 501, "Redis 서버 연결에 실패했습니다."),
  STAT_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), 502, "통계 데이터 업데이트 중 오류가 발생했습니다."),

  LOCATION_NOT_FOUND(HttpStatus.NOT_FOUND.value(), 404,  "위치 정보를 찾을 수 없습니다.");

  private final Integer httpStatus;
  private final Integer code;
  private final String message;
}
