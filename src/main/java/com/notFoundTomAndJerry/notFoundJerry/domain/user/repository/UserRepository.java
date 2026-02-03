package com.notFoundTomAndJerry.notFoundJerry.domain.user.repository;

import com.notFoundTomAndJerry.notFoundJerry.domain.user.entity.User;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

  // 이메일로 유저 찾기 (로그인 시 필요)
  Optional<User> findByEmail(String email);

  // 닉네임 중복 체크용 (회원가입 / 정보 수정 시 중복 방지)
  boolean existsByNickname(String nickname);

  // ID와 닉네임 골라서 조회
  @Query("SELECT u.id, u.nickname FROM User u WHERE u.id IN :ids")
  List<Object[]> findIdAndNicknames(@Param("ids") Collection<Long> ids);

  // 위 결과를 받아서 보기 편한 Map<ID, Nickname>으로 변환
  default Map<Long, String> findNicknameMap(Collection<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return Collections.emptyMap();
    }
    return findIdAndNicknames(ids).stream()
        .collect(Collectors.toMap(
            row -> (Long) row[0],    // Key: User ID
            row -> (String) row[1]   // Value: Nickname
        ));
  }
}