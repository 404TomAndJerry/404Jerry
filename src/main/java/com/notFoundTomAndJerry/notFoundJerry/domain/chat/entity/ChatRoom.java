package com.notFoundTomAndJerry.notFoundJerry.domain.chat.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


/**
 * 채팅방(chatroom)이 사라지면 그안에 수많은 채팅 메세지들도 사라지게 해야됨 철저히 다대일 단방향 고수
 */
@Entity
@Getter
@Table(name = "chat_rooms")
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

/* TODO: Room 엔티티 추가 되었을 때 주석 처리 해제 및 의존성 추가
  // 게임방과 채팅방의 생명주기는 같다. 룸상태가 running이 되면 채팅방은 삭제
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "room_id", nullable = false)
  private Room room;
*/


}

