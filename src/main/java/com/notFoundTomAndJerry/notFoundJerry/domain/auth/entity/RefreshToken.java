package com.notFoundTomAndJerry.notFoundJerry.domain.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 로그인 세션 유지를 위한 Refresh Token
@Entity
@Table(name = "refresh_tokens")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId; // 토큰 소유자 식별용

  @Column(nullable = false, length = 255)
  private String token;

  @Column(nullable = false)
  private LocalDateTime expiredAt;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  public boolean isExpired(LocalDateTime now) {
    return now.isAfter(expiredAt);
  }
}