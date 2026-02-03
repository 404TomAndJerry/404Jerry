package com.notFoundTomAndJerry.notFoundJerry.domain.chat.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notFoundTomAndJerry.notFoundJerry.domain.chat.dto.ChatMessageDto;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.BusinessException;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.CommonErrorCode;
import java.util.ArrayList;
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
  private final ObjectMapper objectMapper; // JSON 변환기 주입

  // 🔥핵심 로직(메세지 저장 및 개수 제한)
  public void sendMessage(Long roomId, ChatMessageDto messageDto) {
    String key = CHAT_ROOM_KEY_PREFIX + roomId;

    try {
      // 객체 -> JSON 문자열 변환 (안전한 저장)
      String value = objectMapper.writeValueAsString(messageDto);

      // Rpush, 리스트의 오른쪽(끝)에 새 메세지 추가
      redisTemplate.opsForList().rightPush(key, value);
      // 레디스 만료일, 3일 만료
      redisTemplate.expire(key, 3, TimeUnit.DAYS);
      // 리스트의 크기를 최신 100개를 유지, 맨 뒤에서 100번째, 맨뒤요소
      redisTemplate.opsForList().trim(key, -100, -1);

    } catch (JsonProcessingException e) {
      // 1. 로그는 상세하게 남겨서 서버 콘솔에서 확인
      log.error("Redis JSON 변환 실패: {}", e.getMessage());
      // 2. 공통 예외 객체를 던져서 GlobalExceptionHandler가 처리하게 함
      throw new BusinessException(CommonErrorCode.REDIS_PROCESSING_ERROR, "메시지 직렬화 중 오류 발생");
    } catch (Exception e){
      // Redis 연결 오류 등 기타 예외 처리
      log.error("Redis 저장 중 알 수 없는 오류: {}", e.getMessage());
      throw new BusinessException(CommonErrorCode.SERVER_ERROR, "Redis 메시지 저장 실패");
    }

  }

  // 전체 조회 (최신 100개)
  public List<ChatMessageDto> findAll(Long roomId) {
    String key = CHAT_ROOM_KEY_PREFIX + roomId;
    List<Object> rawList = redisTemplate.opsForList().range(key, 0, -1);

    if (rawList == null || rawList.isEmpty()) {
      return new ArrayList<>();
    }

    List<ChatMessageDto> dtoList = new ArrayList<>();
    for (Object raw : rawList) {
      try {
        // JSON 문자열 -> 객체 변환 (안전한 조회)
        ChatMessageDto dto = objectMapper.readValue((String) raw, ChatMessageDto.class);
        dtoList.add(dto);
      } catch (JsonProcessingException e) {
        log.error("Redis 메시지 파싱 실패: {}", e.getMessage());
        throw new BusinessException(CommonErrorCode.REDIS_PROCESSING_ERROR, "데이터 복구 중 오류 발생");
      }
    }
    return dtoList;
  }

  // 채팅방 삭제 (Redis 데이터 정리)
  public void deleteChatRoom(Long roomId) {
    String key = CHAT_ROOM_KEY_PREFIX + roomId;
    redisTemplate.delete(key);
  }

}
