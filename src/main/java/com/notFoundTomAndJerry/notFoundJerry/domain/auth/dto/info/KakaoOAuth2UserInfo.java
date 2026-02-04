package com.notFoundTomAndJerry.notFoundJerry.domain.auth.dto.info;

import java.util.Map;

public class KakaoOAuth2UserInfo implements OAuth2UserInfo {

  private final Map<String, Object> attributes;
  private final Map<String, Object> kakaoAccount;
  private final Map<String, Object> profile;

  public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
    this.attributes = attributes;
    this.kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
    // 프로필 권한이 없을 수도 있으므로 체크 필요 (보통은 있음)
    this.profile = (Map<String, Object>) kakaoAccount.get("profile");
  }

  @Override
  public String getProviderId() {
    return String.valueOf(attributes.get("id"));
  }

  @Override
  public String getProvider() {
    return "kakao";
  }

  @Override
  public String getEmail() {
    return (String) kakaoAccount.get("email");
  }

  @Override
  public String getName() {
    if (profile != null && profile.get("nickname") != null) {
      return (String) profile.get("nickname");
    }
    return "Unknown_Kakao"; // 닉네임 동의 안 했을 경우 대비
  }

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }
}