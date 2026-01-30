package com.notFoundTomAndJerry.notFoundJerry.domain.location.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LocationFilterResponse {

  // 영역 내에 존재하는 유효한 location ID 목록 반환
  private List<Long> locationIds;
}