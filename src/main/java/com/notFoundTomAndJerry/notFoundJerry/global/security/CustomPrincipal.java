package com.notFoundTomAndJerry.notFoundJerry.global.security;

import com.notFoundTomAndJerry.notFoundJerry.domain.auth.entity.enums.ProviderType;
import com.notFoundTomAndJerry.notFoundJerry.domain.user.entity.User;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Getter
public class CustomPrincipal implements UserDetails, OAuth2User {

  private final Long userId;
  private final String email;
  private final ProviderType providerType;
  private final Collection<? extends GrantedAuthority> authorities;
  private Map<String, Object> attributes; // OAuth2 로그인 시에만 값 있음

  // 생성자: OAuth2 로그인 용
  public CustomPrincipal(User user, Map<String, Object> attributes) {
    this.userId = user.getId();
    this.email = user.getEmail();
    this.providerType = user.getProviderType();
    this.authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
    this.attributes = attributes;
  }

  // 생성자: JWT 필터 인증 용 (attributes 없음)
  public CustomPrincipal(Long userId, String email, ProviderType providerType) {
    this.userId = userId;
    this.email = email;
    this.providerType = providerType;
    this.authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
  }

  // --- OAuth2User 구현 ---
  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public String getName() {
    return email;
  }

  // --- UserDetails 구현 ---
  @Override
  public String getUsername() { return email; } // 우리는 email을 ID로 씀
  @Override
  public String getPassword() { return null; } // 소셜로그인은 비번 없음
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
  @Override
  public boolean isAccountNonExpired() { return true; }
  @Override
  public boolean isAccountNonLocked() { return true; }
  @Override
  public boolean isCredentialsNonExpired() { return true; }
  @Override
  public boolean isEnabled() { return true; }
}