package com.notFoundTomAndJerry.notFoundJerry.domain.auth.dto.info;

import java.util.Map;

public interface OAuth2UserInfo {
  String getProviderId(); // 소셜 식별자 (google의 sub 등)
  String getProvider();   // google, kakao, naver
  String getEmail();
  String getName();
  Map<String, Object> attributes();
}