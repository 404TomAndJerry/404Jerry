package com.notFoundTomAndJerry.notFoundJerry.domain.location.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LocationDistanceResponse {

  // converter에서 가공한 거리, 단위 전달
  private Double distance;
  private String unit; // m or km
}