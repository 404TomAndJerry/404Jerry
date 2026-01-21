package com.notFoundTomAndJerry.notFoundJerry.domain.chat.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_messages", indexes = {
    @Index(name = "idx_chat_message_created_at", columnList = "chat_room_id,createAt")})
@EntityListeners(AuditingEntityListener.class)
public class ChatMessage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 어느 채팅 룸(룸id)에 연결되었는지
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "chat_room_id", nullable = false)
  private ChatRoom chatRoom;

  // 어떤 유저(유저id)가 작성했는지, 유저 id만 받아오기
  @Column(name = "sender_id", nullable = false) // JoinColumn 대신 단순 ID 값 권장
  private Long senderId;

  //일반 엔터는 무시, 스페이스 엔터는 허용
  @Lob
  @Column(columnDefinition = "TEXT", nullable = false)
  private String message;

  @CreatedDate
  @Column(updatable = false)
  private LocalDateTime createdAt;

}
