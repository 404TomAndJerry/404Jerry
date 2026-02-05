package com.notFoundTomAndJerry.notFoundJerry.domain.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notFoundTomAndJerry.notFoundJerry.domain.chat.dto.ChatMessageDto;
import com.notFoundTomAndJerry.notFoundJerry.domain.chat.entity.ChatMessage;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.BusinessException;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisSubscriber {

  private final ObjectMapper objectMapper;
  private final SimpMessageSendingOperations messagingTemplate;

  // Redis에서 메시지가 오면 이 메서드가 실행됩니다.
  public void sendMessage(String publishMessage) {
    try {
      // 1. JSON 문자열 -> 자바 객체 변환
      ChatMessageDto chatMessage = objectMapper.readValue(publishMessage, ChatMessageDto.class);

      // 2. 실제 구독자(클라이언트)에게 웹소켓으로 발송
      // Destination: /sub/chat/rooms/{roomId}
      messagingTemplate.convertAndSend(
          "/sub/chat/room/" + chatMessage.getRoomId(),
          chatMessage
      );

    } catch (Exception e) {
      log.error("Redis 메시지 파싱 실패", e);
      throw new BusinessException(CommonErrorCode.SERVER_ERROR, "Redis 메시지 파싱 실패");
    }
  }
}
