package com.notFoundTomAndJerry.notFoundJerry.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Access / Refresh Token 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenResponse {

  private String grantType;    // 보통 "Bearer"
  private String accessToken;  // JWT Access Token
  private String refreshToken; // JWT Refresh Token
  private Long accessTokenExpiresIn; // 액세스 토큰 만료 시간 (밀리초)
}