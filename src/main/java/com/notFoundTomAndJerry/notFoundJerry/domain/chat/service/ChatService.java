package com.notFoundTomAndJerry.notFoundJerry.domain.chat.service;

import com.notFoundTomAndJerry.notFoundJerry.domain.chat.dto.ChatMessageDto;
import com.notFoundTomAndJerry.notFoundJerry.domain.chat.entity.ChatMessage;
import com.notFoundTomAndJerry.notFoundJerry.domain.chat.entity.ChatRoom;
import com.notFoundTomAndJerry.notFoundJerry.domain.chat.repository.ChatMessageRedisRepository;
import com.notFoundTomAndJerry.notFoundJerry.domain.chat.repository.ChatMessageRepository;
import com.notFoundTomAndJerry.notFoundJerry.domain.chat.repository.ChatRoomRepository;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.BusinessException;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.domain.ChatErrorCode;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

  private final ChatMessageRepository chatMessageRepository;
  private final ChatRoomRepository chatRoomRepository;
  private final ChatMessageRedisRepository chatMessageRedisRepository;
  private final RedisTemplate<String, Object> redisTemplate; // Redis

  @Transactional
  public void sendMessage(Long roomId, Long senderId, String content) {
    // 1. 방 존재 여부 먼저 확인
    ChatRoom room = getChatRoomId(roomId);

    // RDB 저장 (영구 보관용 - 엔티티 생성 및 저장)
    ChatRoom chatRoom = chatRoomRepository.getReferenceById(room.getId());
    ChatMessage chatMessage = ChatMessage.builder()
        .chatRoom(chatRoom)
        .senderId(senderId)
        .message(content)
        .build();
    chatMessageRepository.save(chatMessage);

    // 레디스에 캐싱하는데 dto로 바꾸어서 집어 넣기, Redis 캐싱 & Pub/Sub
    ChatMessageDto messageDto = ChatMessageDto.from(chatMessage);

    // 레디스에 저장(최신 100개 유지)
    chatMessageRedisRepository.sendMessage(room.getId(), messageDto);

    // 실시간 전송 (구독자들에게)
    redisTemplate.convertAndSend(chatMessageRedisRepository.generateKey(room.getId()), messageDto);
  }


  // 초기 100개 가져오기
  public List<ChatMessageDto> getMessages(Long roomId) {
    // 1. 방 존재 여부 먼저 확인
    ChatRoom room = getChatRoomId(roomId);
    // 레디스에서 먼저 조회, 여기에는 100개만 저장되어있음.
    List<ChatMessageDto> redisMessages = chatMessageRedisRepository.findAll(room.getId());

    // Redis에 데이터가 있으면 바로 반환
    if (!redisMessages.isEmpty()) {
      return redisMessages;
    }
    // 없으면 RDB 조회
    List<ChatMessage> dbMessages = chatMessageRepository.findTop100ByChatRoomIdOrderByCreatedAtDesc(
        room.getId());

    List<ChatMessageDto> result = dbMessages.stream()
        .map(ChatMessageDto::from)
        .collect(Collectors.toList());

    Collections.reverse(result); // 뒤집기 (과거 -> 최신)
    return result;
  }

  // 무한 스크롤 (과거 내역 더보기), 무한스크롤은 프론트가 처리
  public Slice<ChatMessageDto> getPastMessages(Long roomId, Long lastMessageId) {
    // 1. 방 존재 여부 먼저 확인
    ChatRoom room = getChatRoomId(roomId);
    // RDB에서 페이징 쿼리로 가져옴
    Long cursorId = (lastMessageId == null) ? Long.MAX_VALUE : lastMessageId;
    Slice<ChatMessage> messages = chatMessageRepository.findTop20ByChatRoomIdAndIdLessThanOrderByCreatedAtDesc(
        room.getId(), cursorId);
    return messages.map(ChatMessageDto::from);
  }

  /**
   * 게임 종료 시 채팅방 '내용' 초기화
   * (방 엔티티는 살려두고, 메시지만 정리)
   */
  @Transactional
  public void resetChatRoom(Long roomId) {
    // 방 존재 여부 먼저 확인
    ChatRoom room = getChatRoomId(roomId);
    // 1. Redis 캐시 삭제 (가장 중요 - 실시간 채팅창 클리어)
    chatMessageRedisRepository.deleteChatRoom(room.getId());
    // 2. DB 메시지 처리
    chatMessageRepository.DeleteAllByRoomId(room.getId());
    log.info("Redis 캐시 삭제, DB 메세지 삭제");

  }

  private ChatRoom getChatRoomId(Long roomId) {
    // 1. 방 존재 여부 먼저 확인 (방이 없으면 명확한 Custom Exception 발생)
    ChatRoom room = chatRoomRepository.findById(roomId)
        .orElseThrow(() -> new BusinessException(ChatErrorCode.CHAT_ROOM_NOT_FOUND_ID,
            String.format(ChatErrorCode.CHAT_ROOM_NOT_FOUND_ID.getMessage(), roomId)));
    return room;
  }

}
