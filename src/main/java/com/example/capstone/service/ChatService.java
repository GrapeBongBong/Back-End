package com.example.capstone.service;

import com.example.capstone.dto.ChatRoomDTO;
import com.example.capstone.entity.ChatRoom;
import com.example.capstone.entity.ExchangePost;
import com.example.capstone.entity.UserEntity;
import com.example.capstone.repository.ChatRoomRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class ChatService {

    private ObjectMapper objectMapper;
    private final ChatRoomRepository chatRoomRepository;

    public List<ChatRoom> getAllRooms() {
        return chatRoomRepository.findAll();
    }

    public ChatRoom findRoomById(Long roomId) { return chatRoomRepository.findChatRoomByRoomId(roomId); }

    public ChatRoomDTO createRoom(UserEntity user1, UserEntity user2, ExchangePost exchangePost) {
        // user1 >> 게시글 작성자
        // user2 >> 해당 게시글에 신청한 사람

        ChatRoom chatRoom = new ChatRoom();
        String roomName = user2.getNickName() + " (" + user2.getNickName() + ")";
        chatRoom.setRoomName(roomName);
        chatRoom.setDate(LocalDateTime.now());
        chatRoom.setPostWriter(user1);
        chatRoom.setApplicant(user2);
        chatRoom.setExchangePost(exchangePost);

        chatRoomRepository.save(chatRoom);

        return ChatRoomDTO.builder()
                .roomId(chatRoom.getRoomId())
                .roomName(roomName)
                .build();
    }

    public <T> void sendMessage(WebSocketSession session, T message) {
        try{
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
