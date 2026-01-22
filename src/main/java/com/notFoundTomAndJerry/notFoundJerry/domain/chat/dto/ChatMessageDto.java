package com.notFoundTomAndJerry.notFoundJerry.domain.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.notFoundTomAndJerry.notFoundJerry.domain.chat.entity.ChatMessage;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ChatMessageDto {

  private Long id;
  private Long senderId;
  private String message;

  @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdAt;

  public static ChatMessageDto from(ChatMessage entity) {
    return ChatMessageDto.builder()
        .id(entity.getId())
        .senderId(entity.getSenderId())
        .message(entity.getMessage())
        .createdAt(entity.getCreatedAt())
        .build();
  }
}