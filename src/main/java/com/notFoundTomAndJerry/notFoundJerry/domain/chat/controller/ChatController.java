package com.notFoundTomAndJerry.notFoundJerry.domain.chat.controller;

import com.notFoundTomAndJerry.notFoundJerry.domain.chat.dto.ChatMessageDto;
import com.notFoundTomAndJerry.notFoundJerry.domain.chat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatController {

  private final ChatService chatService;

  @MessageMapping("/chat/message")
  public void sendMessage(@Payload ChatMessageDto messageDto) {
    log.info("STOMP 메시지 수신: room={}, msg={}", messageDto.getRoomId(), messageDto.getMessage());

    chatService.sendMessage(
        messageDto.getRoomId(),
        messageDto.getSenderId(),
        messageDto.getMessage()
    );
  }

  @GetMapping("/api/chat/rooms/{roomId}/messages")
  @Operation(summary = "채팅방 조회", description = "채팅방 조회 합니다.")
  public ResponseEntity<List<ChatMessageDto>> getInitialMessages(@PathVariable Long roomId) {
    List<ChatMessageDto> messages = chatService.getMessages(roomId);
    return ResponseEntity.ok(messages);
  }

  @GetMapping("/api/chat/rooms/{roomId}/messages/history")
  @Operation(summary = "채팅창 무한 스크롤", description = "기존 100개 이후에는 한개식 미리 로드.")
  public ResponseEntity<Slice<ChatMessageDto>> getPastMessages(
      @PathVariable Long roomId,
      @RequestParam(required = false) Long lastMessageId // 커서 ID (없으면 처음으로 간주)
  ) {
    Slice<ChatMessageDto> pastMessages = chatService.getPastMessages(roomId, lastMessageId);
    return ResponseEntity.ok(pastMessages);
  }

  @DeleteMapping("/api/chat/rooms/{roomId}")
  @Operation(summary = "방 ID로 채팅방 삭제", description = "방 ID로 채팅방 삭제")
  public ResponseEntity<Void> deleteChatRoom(@PathVariable Long roomId) {
    chatService.resetChatRoom(roomId);
    return ResponseEntity.ok().build();
  }

}
