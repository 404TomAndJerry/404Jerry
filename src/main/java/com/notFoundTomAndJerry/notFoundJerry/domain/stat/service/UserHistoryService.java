package com.notFoundTomAndJerry.notFoundJerry.domain.stat.service;

import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.enums.PlayerRole;
import com.notFoundTomAndJerry.notFoundJerry.domain.stat.entity.UserHistory;
import com.notFoundTomAndJerry.notFoundJerry.domain.stat.repository.UserHistoryRepository;
import com.notFoundTomAndJerry.notFoundJerry.domain.user.entity.User;
import com.notFoundTomAndJerry.notFoundJerry.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserHistoryService {

  private final UserHistoryRepository userHistoryRepository;
  private final UserRepository userRepository;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public UserHistory saveGameResult(Long userId, boolean isWinner, PlayerRole role, boolean isRunaway) {
    UserHistory history = userHistoryRepository.findById(userId)
        .orElseGet(() -> createNewHistory(userId));

    // 도메인 엔티티 내의 비즈니스 메서드 호출 (Pre-aggregation)
    history.updateGameResult(isWinner, role, isRunaway);

    return history; // Dirty Checking으로 자동 저장됨
  }

  private UserHistory createNewHistory(Long userId) {
    User user = userRepository.getReferenceById(userId); // 프록시 조회로 쿼리 절약
    return userHistoryRepository.save(new UserHistory(user));
  }
}