package com.notFoundTomAndJerry.notFoundJerry.domain.auth.repository;

import com.notFoundTomAndJerry.notFoundJerry.domain.auth.entity.OAuthAccount;
import com.notFoundTomAndJerry.notFoundJerry.domain.auth.entity.enums.OAuthProvider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {

  // provider + providerId로 OAuth 계정 조회
  Optional<OAuthAccount> findByProviderAndProviderId(OAuthProvider provider, String providerId);

  // 특정 유저가 연동한 OAuth 계정 목록 조회 (선택)
  Optional<OAuthAccount> findByUserId(Long userId);
}