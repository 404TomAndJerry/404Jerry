package com.notFoundTomAndJerry.notFoundJerry.global.exception.domain;

import com.notFoundTomAndJerry.notFoundJerry.global.exception.ApiCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum LocationErrorCode implements ApiCode {

  // 400 Bad Request
  INVALID_LOCATION_DATA(HttpStatus.BAD_REQUEST.value(), 4500, "유효하지 않은 장소 데이터 형식입니다."),
  INVALID_COORDINATES(HttpStatus.BAD_REQUEST.value(), 4501, "잘못된 위경도 좌표값입니다."),

  // 404 Not Found
  LOCATION_NOT_FOUND(HttpStatus.NOT_FOUND.value(), 4504, "해당 장소를 찾을 수 없습니다."),
  REGION_NOT_FOUND(HttpStatus.NOT_FOUND.value(), 4505, "해당 행정 구역 정보를 찾을 수 없습니다."),

  // 500 Internal Server Error (Batch/External API)
  EXTERNAL_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), 5500,
      "공공데이터 외부 API 호출 중 오류가 발생했습니다."),
  DATA_MAPPING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), 5501, "위치 데이터 변환 중 오류가 발생했습니다.");

  private final Integer httpStatus;
  private final Integer code;
  private final String message;
}