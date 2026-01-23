package com.notFoundTomAndJerry.notFoundJerry.domain.auth.dto.request;

import com.notFoundTomAndJerry.notFoundJerry.domain.auth.entity.enums.OAuthProvider;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OAuthLoginRequest {

  private OAuthProvider provider; // KAKAO, NAVER, GOOGLE
  private String authorizationCode; // OAuth 인가 코드
}