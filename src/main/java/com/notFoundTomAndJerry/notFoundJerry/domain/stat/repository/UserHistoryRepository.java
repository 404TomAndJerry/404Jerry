package com.notFoundTomAndJerry.notFoundJerry.domain.stat.repository;

import com.notFoundTomAndJerry.notFoundJerry.domain.stat.entity.UserHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserHistoryRepository extends JpaRepository<UserHistory,Long> {
  // 기본 기본 사용
}
