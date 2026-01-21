package com.notFoundTomAndJerry.notFoundJerry.domain.chat.repository;

import com.notFoundTomAndJerry.notFoundJerry.domain.chat.entity.ChatMessage;
import java.util.List;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

  // 레디스가 텅텅 비어있을때, db에서 최신 메세지 100개를 가지고옴. 생성시간 기준 내림차순 정렬. 즉, 최신순으로 가져옴
  List<ChatMessage> findTop100ByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId);

  // 📜무한 스크롤용, 과거 내역 조회용 레디스에 없는 101번째 이후에 데이터를 페이징으로 가져올때 사용한다. 이러면 전체 카운터새는 시간은 사라진다.
  Slice<ChatMessage> findTop20ByChatRoomIdAndIdLessThanOrderByCreatedAtDesc(Long chatRoomId,
      Long lastMessageId);

}