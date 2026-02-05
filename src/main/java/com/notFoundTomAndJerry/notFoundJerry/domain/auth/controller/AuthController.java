package com.notFoundTomAndJerry.notFoundJerry.domain.auth.controller;

import com.notFoundTomAndJerry.notFoundJerry.domain.auth.dto.request.LoginRequest;
import com.notFoundTomAndJerry.notFoundJerry.domain.auth.dto.request.SignupRequest;
import com.notFoundTomAndJerry.notFoundJerry.domain.auth.dto.request.TokenRefreshRequest;
import com.notFoundTomAndJerry.notFoundJerry.domain.auth.dto.response.TokenResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth API", description = "인증 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @Operation(summary = "로컬 회원가입")
  @PostMapping("/signup")
  public ResponseEntity<String> signup(@RequestBody @Valid SignupRequest request) {
    authService.signup(request);
    return ResponseEntity.ok("회원가입이 완료되었습니다.");
  }

  @Operation(summary = "로컬 로그인")
  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
    TokenResponse response = authService.login(request);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "토큰 재발급")
  @PostMapping("/refresh")
  public ResponseEntity<TokenResponse> refresh(@RequestBody TokenRefreshRequest request) {
    TokenResponse response = authService.refresh(request);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "로그아웃")
  @PostMapping("/logout")
  public ResponseEntity<String> logout(@RequestBody TokenRefreshRequest request) {
    // 로그아웃 시 Refresh Token을 받아서 DB에서 지움
    authService.logout(request.getRefreshToken());
    return ResponseEntity.ok("로그아웃 되었습니다.");
  }
}