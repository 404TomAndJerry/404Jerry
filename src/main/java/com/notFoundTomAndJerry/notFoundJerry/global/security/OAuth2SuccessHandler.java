package com.notFoundTomAndJerry.notFoundJerry.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notFoundTomAndJerry.notFoundJerry.domain.auth.dto.response.TokenResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.auth.entity.RefreshToken;
import com.notFoundTomAndJerry.notFoundJerry.domain.auth.repository.RefreshTokenRepository;
import com.notFoundTomAndJerry.notFoundJerry.domain.user.entity.User;
import com.notFoundTomAndJerry.notFoundJerry.domain.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final JwtProvider jwtProvider;
  private final RefreshTokenRepository refreshTokenRepository;
  private final UserRepository userRepository;
  private final ObjectMapper objectMapper;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

    // 1. 로그인된 유저 정보 가져오기
    CustomPrincipal principal = (CustomPrincipal) authentication.getPrincipal();

    log.info("소셜 로그인 성공! 이메일: {}", principal.getEmail());

    // 2. DB에서 유저 조회하여 닉네임 가져오기
    User user = userRepository.findById(principal.getUserId())
        .orElseThrow(() -> new RuntimeException("유저 정보를 찾을 수 없습니다."));

    // 3. JWT 토큰 생성
    String accessToken = jwtProvider.createAccessToken(
        user.getId(),
        user.getEmail(),
        user.getNickname(),
        user.getProviderType()
    );

    String refreshToken = jwtProvider.createRefreshToken(
        user.getId()
    );

    // 4. Refresh Token DB 저장
    refreshTokenRepository.deleteByUserId(user.getId());
    refreshTokenRepository.save(RefreshToken.builder()
        .userId(user.getId())
        .token(refreshToken)
        .createdAt(LocalDateTime.now())
        .expiredAt(LocalDateTime.now().plusDays(14))
        .build());

    // 5. 응답 전송 (JSON으로 뿌려주기)
    // 실제 운영 시에는 프론트엔드 URL로 리다이렉트(sendRedirect) 해야 함
    // response.sendRedirect("http://localhost:3000/oauth/callback?access=" + accessToken);
    response.setContentType("application/json;charset=UTF-8");
    TokenResponse tokenResponse = TokenResponse.builder()
        .grantType("Bearer")
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .accessTokenExpiresIn(jwtProvider.getAccessTokenExpirationTime())
        .build();

    response.getWriter().write(objectMapper.writeValueAsString(tokenResponse));
  }
}