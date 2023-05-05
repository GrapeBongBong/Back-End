package com.example.capstone.service;

import com.example.capstone.dto.ChatRoomDTO;
import com.example.capstone.entity.ChatMessage;
import com.example.capstone.entity.ChatRoom;
import com.example.capstone.entity.ExchangePost;
import com.example.capstone.entity.UserEntity;
import com.example.capstone.repository.ChatMessageRepository;
import com.example.capstone.repository.ChatRoomRepository;
import com.example.capstone.repository.PostRepository;
import com.example.capstone.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class ChatService {

    private ObjectMapper objectMapper;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    public List<ChatRoom> getAllRooms() {
        return chatRoomRepository.findAll();
    }

    public ChatRoom findRoomById(Long roomId) { return chatRoomRepository.findChatRoomByRoomId(roomId); }

    public ChatRoom createRoom(UserEntity user1, UserEntity user2, ExchangePost exchangePost) {

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setName(user2.getId());
        chatRoom.setDate(LocalDateTime.now());
        chatRoom.setPostWriter(user1);
        chatRoom.setApplicant(user2);
        chatRoom.setExchangePost(exchangePost);

        chatRoomRepository.save(chatRoom);

        return chatRoom;
    }

    /*public <T> void sendMessage(WebSocketSession session, T message) {
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
        } catch (IOException e) {

        }
    }*/

    public void sendMessage(ChatMessage chatMessage) {
        chatMessageRepository.save(chatMessage);
    }
}
