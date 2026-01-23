package com.notFoundTomAndJerry.notFoundJerry.domain.auth.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequest {

  private String email;
  private String password;
  private String nickname;
}