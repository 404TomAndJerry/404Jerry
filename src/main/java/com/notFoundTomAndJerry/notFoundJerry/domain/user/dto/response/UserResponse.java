package com.notFoundTomAndJerry.notFoundJerry.domain.user.dto.response;

import com.notFoundTomAndJerry.notFoundJerry.domain.auth.entity.enums.ProviderType;
import com.notFoundTomAndJerry.notFoundJerry.domain.user.entity.User;
import com.notFoundTomAndJerry.notFoundJerry.domain.user.entity.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

  private Long id;
  private String email;
  private String nickname;
  private UserStatus status;
  private ProviderType providerType;

  public static UserResponse from(User user) {
    return UserResponse.builder()
        .id(user.getId())
        .email(user.getEmail())
        .nickname(user.getNickname())
        .status(user.getStatus())
        .providerType(user.getProviderType())
        .build();
  }
}