package com.notFoundTomAndJerry.notFoundJerry.domain.chat.repository;

import com.notFoundTomAndJerry.notFoundJerry.domain.chat.dto.ChatMessageDto;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
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
  private static final String CHAT_ROOM_KEY_PREFIX = "chat:room:";
  private final RedisTemplate<String, Object> redisTemplate;

  // 🔥핵심 로직(메세지 저장 및 개수 제한)
  public void sendMessage(Long roomId, ChatMessageDto messageDto) {
    String key = CHAT_ROOM_KEY_PREFIX + roomId;

    // Rpush, 리스트의 오른쪽(끝)에 새 메세지 추가
    redisTemplate.opsForList().rightPush(key, messageDto);

    // 리스트의 크기를 최신 100개를 유지, 맨 뒤에서 100번째, 맨뒤요소
    redisTemplate.opsForList().trim(key, -100, -1);

    // 레디스 만료일, 3일 만료
    redisTemplate.expire(key, 3, TimeUnit.DAYS);
  }

  // 전체 조회 (최신 100개)
  public List<ChatMessageDto> findAll(Long roomId) {
    String key = CHAT_ROOM_KEY_PREFIX + roomId;

    List<Object> rawList = redisTemplate.opsForList().range(key, 0, -1);

    if (rawList == null || rawList.isEmpty()) {
      return List.of();
    }

    try {
      return rawList.stream()
          .map(obj -> (ChatMessageDto) obj) // GenericJackson2Json... 설정 시 자동 캐스팅
          .collect(Collectors.toList());
    } catch (ClassCastException e) {
      log.error("Redis 데이터 변환 실패: Redis 설정을 확인하세요.", e);
      return List.of();
    }
  }

  // 채팅방 삭제 (Redis 데이터 정리)
  public void deleteChatRoom(Long roomId) {
    String key = CHAT_ROOM_KEY_PREFIX + roomId;
    redisTemplate.delete(key);
  }

}
