package com.notFoundTomAndJerry.notFoundJerry.domain.auth.service;

import com.notFoundTomAndJerry.notFoundJerry.domain.auth.dto.info.GoogleOAuth2UserInfo;
import com.notFoundTomAndJerry.notFoundJerry.domain.auth.dto.info.OAuth2UserInfo;
import com.notFoundTomAndJerry.notFoundJerry.domain.auth.entity.OAuthAccount;
import com.notFoundTomAndJerry.notFoundJerry.domain.auth.entity.enums.OAuthProvider;
import com.notFoundTomAndJerry.notFoundJerry.domain.auth.entity.enums.ProviderType;
import com.notFoundTomAndJerry.notFoundJerry.domain.auth.repository.OAuthAccountRepository;
import com.notFoundTomAndJerry.notFoundJerry.domain.user.entity.User;
import com.notFoundTomAndJerry.notFoundJerry.domain.user.repository.UserRepository;
import com.notFoundTomAndJerry.notFoundJerry.global.security.CustomPrincipal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final UserRepository userRepository;
  private final OAuthAccountRepository oauthAccountRepository;

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    // 1. 소셜 서비스(구글 등)에서 유저 정보 가져오기
    OAuth2User oAuth2User = super.loadUser(userRequest);

    // 2. 어떤 소셜인지 확인 (google, naver, kakao)
    String registrationId = userRequest.getClientRegistration().getRegistrationId();

    OAuth2UserInfo oAuth2UserInfo = null;
    if (registrationId.equals("google")) {
      oAuth2UserInfo = new GoogleOAuth2UserInfo(oAuth2User.getAttributes());
    }
    // else if (registrationId.equals("naver")) { ... }
    // else if (registrationId.equals("kakao")) { ... }
    else {
      log.error("지원하지 않는 소셜 로그인입니다.");
      throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다.");
    }

    // 3. DB 저장 또는 업데이트
    User user = saveOrUpdate(oAuth2UserInfo);

    // 4. CustomPrincipal(통합 유저 객체) 반환 -> SecurityContext에 저장됨
    return new CustomPrincipal(user, oAuth2User.getAttributes());
  }

  private User saveOrUpdate(OAuth2UserInfo userInfo) {
    // OAuthAccount 테이블에서 먼저 조회 (provider + providerId)
    OAuthProvider provider = OAuthProvider.valueOf(userInfo.getProvider().toUpperCase());

    return oauthAccountRepository.findByProviderAndProviderId(provider, userInfo.getProviderId())
        .map(OAuthAccount::getUser)
        .orElseGet(() -> {
          // 계정이 없으면 신규 생성
          // 닉네임 중복 방지 로직 (간단 예시)
          String nickname = userInfo.getName() + "_" + UUID.randomUUID().toString().substring(0, 5);

          User newUser = User.builder()
              .email(userInfo.getEmail())
              .nickname(nickname)
              .providerType(ProviderType.valueOf(userInfo.getProvider().toUpperCase()))
              .createdAt(LocalDateTime.now())
              // User 엔티티 필드에 맞춰서 채워넣으세요
              .build();
          userRepository.save(newUser);

          OAuthAccount newAccount = OAuthAccount.builder()
              .user(newUser)
              .provider(provider)
              .providerId(userInfo.getProviderId())
              .createdAt(LocalDateTime.now())
              .build();
          oauthAccountRepository.save(newAccount);

          return newUser;
        });
  }
}