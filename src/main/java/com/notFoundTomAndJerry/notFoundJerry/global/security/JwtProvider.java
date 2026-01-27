
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
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtProvider {

    private final SecretKey accSecretKey;
    private final SecretKey refSecretKey;
    private final long accessTokenExpirationTime;
    private final long refreshTokenExpirationTime;

    // 액서스 와 리프레쉬 시크릿 키값 다르게
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


    public String createAccessToken(Long userId,String loginId, String nickname, ProviderType provider, String userCode)  {
        return Jwts.builder()
                //표준 Claim, 토큰의 주인 식별자
                .setSubject(String.valueOf(userId))

                // custom claim
                .claim("loginId", loginId)
                .claim("nickname", nickname)
                .claim("provider", provider)
                .claim("userCode", userCode)

                //시간 claim
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpirationTime))

                .signWith(accSecretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(Long userId, String loginId, String userCode) {
        return Jwts.builder()
                //표준 Claim, 토큰의 주인 식별자
                .setSubject(String.valueOf(userId))

                // custom claim
                .claim("loginId", loginId)
                .claim("userCode", userCode)

                //시간 claim
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpirationTime))

                .signWith(refSecretKey, SignatureAlgorithm.HS256)
                .compact();

    }
    // 1. Access Token 검증용 (accSecretKey 사용)
    public boolean validateAccessToken(String token) {
        return validateToken(token, accSecretKey);
    }

    // 2. Refresh Token 검증용 (refSecretKey 사용)
    public boolean validateRefreshToken(String token) {
        return validateToken(token, refSecretKey);
    }

    public boolean validateToken(String token, SecretKey secretKey) {
        try {
            Jwts.
                    parserBuilder().
                    setSigningKey(secretKey).
                    build().
                    parseClaimsJws(token);
            return true; //문제 없으면 통과
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("잘못된 JWT 서명입니다."); //깨진 토큰
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다."); //유효기간 지남 -> 재발급 필요
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        } catch (Exception e) {
            log.error("알 수 없는 JWT 에러: {}", e.getMessage()); // ERROR: 서버 문제 (비상)
        }

        return false; // 예외 발생 시 무조건 false 반환
    }

    
    //가져오는것도 두개로 만드러야됨
    public Claims getACCClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(accSecretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Claims getREFClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(refSecretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    //액서스 토큰 블랙리스트 만들기
    public Long getExpiredToken(String token) {
        try {
            Date expiration = Jwts.parserBuilder()
                    .setSigningKey(accSecretKey) // <--- ✅ 여기도 키 설정 추가 필요
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
            long now = new Date().getTime();
            return (expiration.getTime() - now);
        } catch (ExpiredJwtException e) {
            // 이미 만료된 토큰이면 남은 시간은 0 또는 음수
            return -1L;
        }
    }

}

