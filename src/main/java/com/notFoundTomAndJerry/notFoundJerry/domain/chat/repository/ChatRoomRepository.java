package com.notFoundTomAndJerry.notFoundJerry.domain.chat.repository;

import com.notFoundTomAndJerry.notFoundJerry.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
  // 기본 CRUD 제공 (findById, save, delete 등)
}
