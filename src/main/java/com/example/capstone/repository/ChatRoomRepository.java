package com.example.capstone.repository;

import com.example.capstone.entity.ChatRoom;
import com.example.capstone.entity.ExchangePost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    // 채팅방 조회 (roomId 기준)
    ChatRoom findChatRoomByRoomId(Long roomId);

    // 채팅방 조회 (게시글 기준)
    List<ChatRoom> findChatRoomsByExchangePost(ExchangePost exchangePost);
}
