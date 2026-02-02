package com.notFoundTomAndJerry.notFoundJerry.domain.auth.dto.info;

import java.util.Map;

public record GoogleOAuth2UserInfo(Map<String, Object> attributes) implements OAuth2UserInfo {

  @Override
  public String getProviderId() {
    return (String) attributes.get("sub");
  }

  @Override
  public String getProvider() {
    return "google";
  }

  @Override
  public String getEmail() {
    return (String) attributes.get("email");
  }

  @Override
  public String getName() {
    return (String) attributes.get("name");
  }
}