package com.notFoundTomAndJerry.notFoundJerry.domain.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.notFoundTomAndJerry.notFoundJerry.domain.chat.entity.ChatMessage;
import com.notFoundTomAndJerry.notFoundJerry.domain.chat.entity.ChatRoom;
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
  private Long roomId;
  private String message;
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdAt;

  public static ChatMessageDto from(ChatMessage entity) {
    return ChatMessageDto.builder()
        .id(entity.getId())
        .roomId(entity.getChatRoom().getId())
        .senderId(entity.getSenderId())
        .message(entity.getMessage())
        .createdAt(entity.getCreatedAt())
        .build();
  }
}