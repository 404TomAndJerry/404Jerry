package com.notFoundTomAndJerry.notFoundJerry.domain.user.repository;

import com.notFoundTomAndJerry.notFoundJerry.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  // 이메일로 유저 찾기 (로그인 시 필요)
  Optional<User> findByEmail(String email);

  // 닉네임 중복 체크용 (선택)
  boolean existsByNickname(String nickname);
}