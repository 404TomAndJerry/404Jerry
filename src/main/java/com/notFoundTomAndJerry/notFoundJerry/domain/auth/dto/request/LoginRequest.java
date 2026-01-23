package com.notFoundTomAndJerry.notFoundJerry.domain.auth.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRequest {

  private String email;
  private String password;
}