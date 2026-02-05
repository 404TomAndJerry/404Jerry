package com.notFoundTomAndJerry.notFoundJerry.domain.chat.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notFoundTomAndJerry.notFoundJerry.domain.chat.dto.ChatMessageDto;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.BusinessException;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.CommonErrorCode;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.domain.ChatErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ChatMessageRedisRepository {

  // 레디스 네임 컨벤션
  // Redis Key 패턴: chat:room:{roomId}
  private static final String CHAT_ROOM_KEY_PREFIX = "chat:rooms:";
  private final RedisTemplate<String, Object> redisTemplate;
  private final ObjectMapper objectMapper; // JSON 변환기 주입

  // 🔥핵심 로직(메세지 저장 및 개수 제한)
  public void sendMessage(Long roomId, ChatMessageDto messageDto) {
    String key = generateKey(roomId);
    try {
      // Rpush, 리스트의 오른쪽(끝)에 새 메세지 추가
      redisTemplate.opsForList().rightPush(key, messageDto);
      // 레디스 만료일, 3일 만료
      redisTemplate.expire(key, 3, TimeUnit.DAYS);
      // 리스트의 크기를 최신 100개를 유지, 맨 뒤에서 100번째, 맨뒤요소
      redisTemplate.opsForList().trim(key, -100, -1);

    } catch (Exception e) {
      // Redis 연결 오류 등 기타 예외 처리
      log.error("Redis 저장 중 알 수 없는 오류: {}", e.getMessage());
      throw new BusinessException(CommonErrorCode.SERVER_ERROR, "Redis 메시지 저장 실패");
    }

  }

  // 전체 조회 (최신 100개)
  public List<ChatMessageDto> findAll(Long roomId) {
    String key = generateKey(roomId);

    try {
      List<Object> rawList = redisTemplate.opsForList().range(key, 0, -1);

      if (rawList == null || rawList.isEmpty()) {
        return new ArrayList<>();
      }
      return rawList.stream()
          .filter(ChatMessageDto.class::isInstance) // 타입 검증 (ClassCastException 방지)
          .map(ChatMessageDto.class::cast)
          .toList();

    } catch (Exception e) {
      log.error("Redis 메시지 파싱 실패: {}", e.getMessage());
      return new ArrayList<>();
    }
  }

  // 채팅방 삭제 (Redis 데이터 정리)
  public void deleteChatRoom(Long roomId) {
    String key = generateKey(roomId);
    redisTemplate.delete(key);
  }

  public String generateKey(Long roomId) {
    return CHAT_ROOM_KEY_PREFIX + roomId;
  }

}
