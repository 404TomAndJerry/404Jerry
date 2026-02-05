package com.notFoundTomAndJerry.notFoundJerry.domain.chat.entity;

import com.notFoundTomAndJerry.notFoundJerry.domain.room.entity.Room;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;


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
  private Long id;

  /**
   게임방과 채팅방의 생명주기는 같다. 룸상태가 running이 되면 채팅방은 삭제
   room_id에 유니크 제약 조건을 추가하여 1:1 관계를 엄격히 보장
   🔥대기방(Room)이 DB에서 삭제되면, 채팅방(ChatRoom)도 같이 삭제
   소프트 삭제를 지원히지 않음.
   이유는 게임시작전에만 사용하는 것이기에, 신고는 그떄 바로 내용을 따로 저장하는걸로 나중에 추후 합의
   */
  @MapsId
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "room_id", nullable = false, unique = true)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Room room;

}

