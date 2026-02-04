package com.notFoundTomAndJerry.notFoundJerry.domain.auth.dto.info;

import java.util.Map;

public class NaverOAuth2UserInfo implements OAuth2UserInfo {

  private final Map<String, Object> attributes; // 전체 데이터
  private final Map<String, Object> response;   // response 내부 데이터

  public NaverOAuth2UserInfo(Map<String, Object> attributes) {
    this.attributes = attributes;
    // 네이버는 "response" 키 안에 유저 정보가 들어있음
    this.response = (Map<String, Object>) attributes.get("response");
  }

  @Override
  public String getProviderId() {
    return (String) response.get("id");
  }

  @Override
  public String getProvider() {
    return "naver";
  }

  @Override
  public String getEmail() {
    return (String) response.get("email");
  }

  @Override
  public String getName() {
    return (String) response.get("name");
  }

  @Override
  public String getAge() {
    return (String) response.get("age");
  }

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }
}