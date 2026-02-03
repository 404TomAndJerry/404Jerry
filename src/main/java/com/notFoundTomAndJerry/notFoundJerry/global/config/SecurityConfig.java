package com.notFoundTomAndJerry.notFoundJerry.global.config;

import com.notFoundTomAndJerry.notFoundJerry.domain.auth.service.CustomOAuth2UserService;
import com.notFoundTomAndJerry.notFoundJerry.global.security.JwtAuthenticationFilter;
import com.notFoundTomAndJerry.notFoundJerry.global.security.JwtProvider;
import com.notFoundTomAndJerry.notFoundJerry.global.security.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtProvider jwtProvider;
  private final CustomOAuth2UserService customOAuth2UserService;
  private final OAuth2SuccessHandler oAuth2SuccessHandler;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable) // REST API이므로 csrf 보안 필요 없음
        .httpBasic(AbstractHttpConfigurer::disable) // 기본 인증 로그인 비활성화
        .formLogin(AbstractHttpConfigurer::disable) // 기본 폼 로그인 비활성화

        // 세션을 사용하지 않음 (Stateless)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        // 요청에 대한 권한 설정
        .authorizeHttpRequests(auth -> auth
            // 인증 없이 접근 가능한 경로
            .requestMatchers(
                "/auth/**", // 소셜 관련 경로 허용
                "/api/v1/auth/**", // 소셜 관련 경로 허용
                "/login/**", // 소셜 관련 경로 허용
                "/oauth2/**", // 소셜 관련 경로 허용
                "/v3/api-docs/**", // 스웨거
                "/swagger-ui/**", // 스웨거
                "/swagger-ui.html") // 스웨거
            // 그 외 모든 요청은 인증 필요
            .permitAll()
            .anyRequest().authenticated()
        )

        // 소셜 로그인 설정
        .oauth2Login(oauth2 -> oauth2
            .userInfoEndpoint(userInfo -> userInfo
                .userService(customOAuth2UserService) // 유저 정보 가져오는 서비스 등록
            )
            .successHandler(oAuth2SuccessHandler)     // 로그인 성공 시 처리 핸들러 등록
        )

        // JWT 필터 등록
        .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}