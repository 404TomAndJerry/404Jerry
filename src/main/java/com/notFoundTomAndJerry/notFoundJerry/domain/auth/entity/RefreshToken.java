package com.notFoundTomAndJerry.notFoundJerry.domain.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

// 로그인 세션 유지를 위한 Refresh Token
@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private User user;

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