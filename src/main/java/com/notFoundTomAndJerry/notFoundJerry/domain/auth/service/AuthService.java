package com.notFoundTomAndJerry.notFoundJerry.domain.auth.service;

import com.notFoundTomAndJerry.notFoundJerry.domain.auth.dto.request.LoginRequest;
import com.notFoundTomAndJerry.notFoundJerry.domain.auth.dto.request.SignupRequest;
import com.notFoundTomAndJerry.notFoundJerry.domain.auth.dto.request.TokenRefreshRequest;
import com.notFoundTomAndJerry.notFoundJerry.domain.auth.dto.response.TokenResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.auth.entity.RefreshToken;
import com.notFoundTomAndJerry.notFoundJerry.domain.auth.entity.enums.ProviderType;
import com.notFoundTomAndJerry.notFoundJerry.domain.auth.repository.RefreshTokenRepository;
import com.notFoundTomAndJerry.notFoundJerry.domain.user.entity.User;
import com.notFoundTomAndJerry.notFoundJerry.domain.user.entity.enums.UserStatus;
import com.notFoundTomAndJerry.notFoundJerry.domain.user.repository.UserRepository;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.BusinessException;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.domain.AuthErrorCode;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.domain.UserErrorCode;
import com.notFoundTomAndJerry.notFoundJerry.global.security.JwtProvider;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtProvider jwtProvider;
  private final PasswordEncoder passwordEncoder;

  // 1. 로컬 회원가입
  @Transactional
  public void signup(SignupRequest request) {
    // 이메일 중복 검사
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new BusinessException(AuthErrorCode.EMAIL_DUPLICATION);
    }

    // 닉네임 중복 검사
    if (userRepository.existsByNickname(request.getNickname())) {
      throw new BusinessException(UserErrorCode.NICKNAME_DUPLICATION);
    }

    if (userRepository.existsByEmail(request.getEmail())) {
      throw new BusinessException(AuthErrorCode.EMAIL_DUPLICATION);
    }

    User user = User.builder()
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .nickname(request.getNickname())
        .age(request.getAge())
        .status(UserStatus.ACTIVE)
        .providerType(ProviderType.LOCAL)
        .build();

    userRepository.save(user);
  }

  // 2. 로컬 로그인
  @Transactional
  public TokenResponse login(LoginRequest request) {
    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new BusinessException(AuthErrorCode.LOGIN_FAILED));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new BusinessException(AuthErrorCode.LOGIN_FAILED);
    }

    String accessToken = jwtProvider.createAccessToken(
        user.getId(),
        user.getEmail(),
        user.getNickname(),
        user.getProviderType()
    );
    String refreshToken = jwtProvider.createRefreshToken(user.getId());

    refreshTokenRepository.deleteByUserId(user.getId());
    refreshTokenRepository.save(RefreshToken.builder()
        .userId(user.getId())
        .token(refreshToken)
        .createdAt(LocalDateTime.now())
        .expiredAt(LocalDateTime.now().plusDays(14))
        .build());

    return TokenResponse.builder()
        .grantType("Bearer")
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .accessTokenExpiresIn(jwtProvider.getAccessTokenExpirationTime())
        .build();
  }

  // 3. 토큰 재발급
  @Transactional
  public TokenResponse refresh(TokenRefreshRequest request) {
    if (!jwtProvider.validateRefreshToken(request.getRefreshToken())) {
      throw new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
        .orElseThrow(() -> new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN));

    User user = userRepository.findById(storedToken.getUserId())
        .orElseThrow(() -> new BusinessException(AuthErrorCode.USER_NOT_FOUND));

    String newAccessToken = jwtProvider.createAccessToken(
        user.getId(),
        user.getEmail(),
        user.getNickname(),
        user.getProviderType()
    );
    String newRefreshToken = jwtProvider.createRefreshToken(user.getId());

    refreshTokenRepository.delete(storedToken);
    refreshTokenRepository.save(RefreshToken.builder()
        .userId(user.getId())
        .token(newRefreshToken)
        .createdAt(LocalDateTime.now())
        .expiredAt(LocalDateTime.now().plusDays(14))
        .build());

    return TokenResponse.builder()
        .grantType("Bearer")
        .accessToken(newAccessToken)
        .refreshToken(newRefreshToken)
        .accessTokenExpiresIn(jwtProvider.getAccessTokenExpirationTime())
        .build();
  }

  // 4. 로그아웃
  @Transactional
  public void logout(String refreshToken) {
    // 리프레시 토큰을 DB에서 삭제하면 재발급이 불가능해지므로 로그아웃 처리됨
    RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
        .orElseThrow(() -> new BusinessException(AuthErrorCode.INVALID_REFRESH_TOKEN));

    refreshTokenRepository.delete(storedToken);
  }
}