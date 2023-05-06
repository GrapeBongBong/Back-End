package com.example.capstone.service;

import com.example.capstone.dto.ChatRoomDTO;
import com.example.capstone.entity.ChatRoom;
import com.example.capstone.entity.ExchangePost;
import com.example.capstone.entity.UserEntity;
import com.example.capstone.handler.WebSocketSessionManager;
import com.example.capstone.repository.ChatRoomRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private WebSocketSessionManager sessionManager;

    public List<ChatRoom> getAllRooms() {
        return chatRoomRepository.findAll();
    }

    public ChatRoom findRoomById(Long roomId) { return chatRoomRepository.findChatRoomByRoomId(roomId); }

    public ChatRoom createRoom(UserEntity user1, UserEntity user2, ExchangePost exchangePost) {
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

        return chatRoom;

        /*return ChatRoomDTO.builder()
                .roomId(chatRoom.getRoomId())
                .roomName(roomName)
                .build();*/
    }

    public <T> void sendMessage(WebSocketSession session, T message) {
        try{
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void sendMessageToChatRoom(Long chatRoomId, String message) {
        // 채팅방 아이디로 DB 에서 채팅방 정보 조회
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElse(null);

        if (chatRoom != null) {
            // 채팅방에서 연결된 WebSocketSession 가져오기
            WebSocketSession session = sessionManager.getSession(chatRoomId);

            if (session != null && session.isOpen()) {
                try {
                    // 채팅 메시지 전송
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    // 예외 처리
                    e.printStackTrace();
                }
            } else {
                // 세션이 없거나 닫혀있을 경우 처리
                System.out.println("WebSocketSession is not available");
            }
        } else {
            // 채팅방이 존재하지 않을 경우 처리
            System.out.println("ChatRoom not found");
        }
    }
}
