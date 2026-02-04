package com.notFoundTomAndJerry.notFoundJerry.global.security;

import com.notFoundTomAndJerry.notFoundJerry.domain.auth.entity.enums.ProviderType;
import com.notFoundTomAndJerry.notFoundJerry.domain.user.entity.User;
import com.notFoundTomAndJerry.notFoundJerry.domain.user.entity.enums.UserStatus;
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
import java.util.HashMap;
import java.util.List;
import javax.crypto.SecretKey;
import lombok.Getter;
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
  @Getter
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
  public String createAccessToken(Long userId, String email, String nickname, ProviderType provider) {
    return Jwts.builder()
        .setSubject(String.valueOf(userId)) // PK를 Subject로 설정
        .claim("email", email)              // 이메일
        .claim("nickname", nickname)        // 닉네임
        .claim("provider", provider)        // 가입 경로
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpirationTime))
        .signWith(accSecretKey, SignatureAlgorithm.HS256)
        .compact();
  }

  // Refresh Token 생성
  public String createRefreshToken(Long userId) {
    return Jwts.builder()
        .setSubject(String.valueOf(userId))
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpirationTime))
        .signWith(refSecretKey, SignatureAlgorithm.HS256)
        .compact();
  }

  // 토큰에서 CustomPrincipal 객체를 만들어서 넣음
  public Authentication getAuthentication(String token) {
    Claims claims = getACCClaims(token);

    // 1. 토큰에 담긴 정보 꺼내기
    String userIdStr = claims.getSubject();
    Long userId = Long.valueOf(userIdStr);
    String email = claims.get("email", String.class);
    String nickname = claims.get("nickname", String.class);
    // enum 변환 (String -> Enum)
    String providerStr = claims.get("provider", String.class);
    ProviderType providerType = ProviderType.valueOf(providerStr);

    // 2. 임시 User 객체 생성 (DB 조회를 줄이기 위해 토큰 정보로 만듦)
    // 필요한 필드만 채웁니다. (비밀번호 등은 몰라도 됨)
    User user = User.builder()
        .id(userId)
        .email(email)
        .nickname(nickname)
        .providerType(providerType)
        .status(UserStatus.ACTIVE) // 토큰이 있다는 건 ACTIVE라는 뜻
        .build();

    // 3. CustomPrincipal 생성
    CustomPrincipal principal = new CustomPrincipal(user, new HashMap<>());

    // 4. Authentication 객체 반환 (이제 Principal은 String이 아니라 객체임!)
    List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    return new UsernamePasswordAuthenticationToken(principal, "", authorities);
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
}