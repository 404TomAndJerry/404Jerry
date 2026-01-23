package com.notFoundTomAndJerry.notFoundJerry.domain.location.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LocationDistanceResponse {

  private Double distance;
  private String unit; // m or km
}