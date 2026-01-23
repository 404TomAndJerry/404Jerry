package com.notFoundTomAndJerry.notFoundJerry.domain.auth.repository;

import com.notFoundTomAndJerry.notFoundJerry.domain.auth.entity.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  // refresh token 문자열로 조회 (재발급 시 사용)
  Optional<RefreshToken> findByToken(String token);

  // 특정 유저의 refresh token 조회 (로그아웃 / 재로그인 처리용)
  Optional<RefreshToken> findByUserId(Long userId);

  // 특정 유저의 refresh token 삭제
  void deleteByUserId(Long userId);
}