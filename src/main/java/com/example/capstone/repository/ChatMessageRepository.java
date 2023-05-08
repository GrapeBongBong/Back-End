package com.example.capstone.repository;

import com.example.capstone.entity.ChatMessage;
import com.example.capstone.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> getChatMessagesByChatRoom(ChatRoom chatRoom);
}
