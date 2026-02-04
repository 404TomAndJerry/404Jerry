package com.notFoundTomAndJerry.notFoundJerry.domain.user.dto.response;

import com.notFoundTomAndJerry.notFoundJerry.domain.user.entity.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserStatusResponse {
  private String email;
  private UserStatus status;
}