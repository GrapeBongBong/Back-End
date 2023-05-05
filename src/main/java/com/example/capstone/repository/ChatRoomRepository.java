package com.example.capstone.repository;

import com.example.capstone.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    // 채팅방 조회
    List<ChatRoom> findAll();

    ChatRoom findChatRoomByRoomId(Long roomId);
}
