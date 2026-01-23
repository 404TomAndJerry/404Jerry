package com.notFoundTomAndJerry.notFoundJerry.domain.auth.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

// Access Token 재발급 요청 DTO
@Getter
@NoArgsConstructor
public class TokenRefreshRequest {

  private String refreshToken; // Refresh Token 값
}