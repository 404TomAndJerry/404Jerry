package com.notFoundTomAndJerry.notFoundJerry.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Access / Refresh Token 응답 DTO
 */
@Getter
@AllArgsConstructor
public class TokenResponse {

  private String accessToken;  // JWT Access Token
  private String refreshToken; // JWT Refresh Token
}