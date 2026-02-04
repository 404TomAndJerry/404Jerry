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
  public Integer getAge() {
    String ageRange = (String) response.get("age");

    if (ageRange == null) return null;

    // "20-29" 에서 "-" 기준 앞부분인 "20"만 가져와서 숫자로 변환
    try {
      return Integer.parseInt(ageRange.split("-")[0]);
    } catch (NumberFormatException e) {
      return null; // 변환 실패 시 null 처리
    }
  }

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }
}