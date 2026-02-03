package com.notFoundTomAndJerry.notFoundJerry.domain.game.service;

import com.notFoundTomAndJerry.notFoundJerry.domain.game.converter.GameConverter;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.dto.response.RunawayResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.entity.RunawayLog;
import com.notFoundTomAndJerry.notFoundJerry.domain.game.repository.RunawayLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 탈주 처리 서비스
 * - 서버에서 자동 호출
 * - 탈주 시 패배 처리
 * - runaway_logs 기록
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RunawayService {

    private final RunawayLogRepository runawayLogRepository;
    private final GameConverter gameConverter;

    // 탈주 처리, gameId 게임 ID, userId 탈주한 사용자 ID
    @Transactional
    public RunawayResponse handleRunaway(Long gameId, Long userId) {
        // 이미 탈주 기록이 있으면 중복 저장하지 않음 (멱등성)
        if (runawayLogRepository.existsByGameIdAndUserId(gameId, userId)) {
            return gameConverter.toRunawayResponse();
        }
        // 탈주 로그 저장
        RunawayLog runawayLog = RunawayLog.builder()
                .gameId(gameId)
                .userId(userId)
                .build();
        runawayLogRepository.save(runawayLog);

        // Response 생성 (Converter 사용)
        return gameConverter.toRunawayResponse();
    }
}
