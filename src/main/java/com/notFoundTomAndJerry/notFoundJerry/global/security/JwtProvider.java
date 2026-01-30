package com.notFoundTomAndJerry.notFoundJerry.global.security;

import com.notFoundTomAndJerry.notFoundJerry.domain.auth.entity.enums.ProviderType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtProvider {

  private final SecretKey accSecretKey;
  private final SecretKey refSecretKey;
  private final long accessTokenExpirationTime;
  private final long refreshTokenExpirationTime;

  public JwtProvider(
      @Value("${jwt.accsecret}") String accSecretKey,
      @Value("${jwt.refsecret}") String refSecretKey,
      @Value("${jwt.access-token-expiration}") long accessTokenExpirationTime,
      @Value("${jwt.refresh-token-expiration}") long refreshTokenExpirationTime
  ) {
    byte[] accKeyBytes = Decoders.BASE64.decode(accSecretKey);
    byte[] refKeyBytes = Decoders.BASE64.decode(refSecretKey);
    this.accSecretKey = Keys.hmacShaKeyFor(accKeyBytes);
    this.refSecretKey = Keys.hmacShaKeyFor(refKeyBytes);
    this.accessTokenExpirationTime = accessTokenExpirationTime;
    this.refreshTokenExpirationTime = refreshTokenExpirationTime;
  }

  // Access Token 생성
  public String createAccessToken(Long userId, String loginId, String nickname, ProviderType provider, String userCode) {
    return Jwts.builder()
        .setSubject(String.valueOf(userId))
        .claim("loginId", loginId)
        .claim("nickname", nickname)
        .claim("provider", provider)
        .claim("userCode", userCode)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpirationTime))
        .signWith(accSecretKey, SignatureAlgorithm.HS256)
        .compact();
  }

  // Refresh Token 생성
  public String createRefreshToken(Long userId, String loginId, String userCode) {
    return Jwts.builder()
        .setSubject(String.valueOf(userId))
        .claim("loginId", loginId)
        .claim("userCode", userCode)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpirationTime))
        .signWith(refSecretKey, SignatureAlgorithm.HS256)
        .compact();
  }

  // ★ 추가된 메서드: 토큰에서 인증 정보(Authentication) 추출
  public Authentication getAuthentication(String token) {
    Claims claims = getACCClaims(token);
    String userId = claims.getSubject();

    // 권한은 ROLE_USER로 통일 (필요하면 클레임에서 꺼내오도록 수정)
    List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

    // Principal에 userId(PK)를 넣어서 컨트롤러에서 @AuthenticationPrincipal로 꺼내 쓸 수 있게 함
    return new UsernamePasswordAuthenticationToken(userId, "", authorities);
  }

  public boolean validateAccessToken(String token) {
    return validateToken(token, accSecretKey);
  }

  public boolean validateRefreshToken(String token) {
    return validateToken(token, refSecretKey);
  }

  private boolean validateToken(String token, SecretKey secretKey) {
    try {
      Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
      return true;
    } catch (SecurityException | MalformedJwtException e) {
      log.warn("잘못된 JWT 서명입니다.");
    } catch (ExpiredJwtException e) {
      log.info("만료된 JWT 토큰입니다.");
    } catch (UnsupportedJwtException e) {
      log.error("지원되지 않는 JWT 토큰입니다.");
    } catch (IllegalArgumentException e) {
      log.info("JWT 토큰이 잘못되었습니다.");
    }
    return false;
  }

  private Claims getACCClaims(String token) {
    return Jwts.parserBuilder().setSigningKey(accSecretKey).build().parseClaimsJws(token).getBody();
  }

  // Getter for Expiration Time (Service에서 사용하기 위해 추가)
  public long getAccessTokenExpirationTime() {
    return accessTokenExpirationTime;
  }
}