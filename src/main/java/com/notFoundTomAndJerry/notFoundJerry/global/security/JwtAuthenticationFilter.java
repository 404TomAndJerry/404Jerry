package com.notFoundTomAndJerry.notFoundJerry.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  public static final String AUTHORIZATION_HEADER = "Authorization";
  public static final String BEARER_PREFIX = "Bearer ";

  // 아까 만드신 클래스 이름이 JwtProvider였으므로 이걸 주입받아야 합니다.
  private final JwtProvider jwtProvider;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

    // 1. Request Header 에서 토큰을 꺼냄
    String jwt = resolveToken(request);

    // 2. validateToken 으로 토큰 유효성 검사
    if (StringUtils.hasText(jwt) && jwtProvider.validateAccessToken(jwt)) {
      // 3. 토큰이 유효하면 토큰에서 Authentication 객체를 가지고 와서 SecurityContext 에 저장
      Authentication authentication = jwtProvider.getAuthentication(jwt);
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    filterChain.doFilter(request, response);
  }

  // Request Header 에서 토큰 정보 추출
  private String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
      return bearerToken.substring(7);
    }
    return null;
  }
}