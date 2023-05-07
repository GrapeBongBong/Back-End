package com.example.capstone.handler;

import com.example.capstone.dto.ChatMessageDTO;
import com.example.capstone.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;

@RequiredArgsConstructor
@Component
@Slf4j
public class ChatHandler extends TextWebSocketHandler { // Client 가 Send 할 수 있는 경로

    private final ObjectMapper objectMapper;
    private final ChatService chatService;

    // 각 채팅방마다 세션 관리
    // key: 채팅방 ID, value: WebSocket 세션
    private static Map<Long, List<WebSocketSession>> chatSessions = new HashMap<>();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("payload {}", payload);

        ChatMessageDTO chatMessageDTO = objectMapper.readValue(payload, ChatMessageDTO.class); // 보내온 json 데이터를 chatMessage.class 에 맞게 파싱
        log.info("session {}", chatMessageDTO.toString());

        Long chatRoomId = extractChatRoomIdFromSession(session);

        // 보내온 메시지를 DB 에 저장
        chatService.saveMessage(chatMessageDTO, chatRoomId);

        // 해당 채팅방의 세션들에게 메시지 전송
        List<WebSocketSession> roomSessions = chatSessions.getOrDefault(chatRoomId, new ArrayList<>());
        for (WebSocketSession roomSession: roomSessions) {
            roomSession.sendMessage(message);
        }
    }

    // Client 접속 시 호출되는 메서드
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 클라이언트의 요청에서 채팅방 ID 추출
        Long chatRoomId = extractChatRoomIdFromSession(session);

        // 채팅방에 해당하는 세션 리스트를 가져옴
        List<WebSocketSession> roomSessions = chatSessions.getOrDefault(chatRoomId, new ArrayList<>());

        // 현재 세션을 해당 채팅방의 세션 리스트에 추가
        roomSessions.add(session);

        // 업데이트된 세션 리스트를 맵에 다시 저장
        chatSessions.put(chatRoomId, roomSessions);
        log.info("WebSocket connection established for chat room: {}", chatRoomId);
    }

    // Client 접속 해제 시 호출되는 메서드
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 클라이언트의 요청에서 채팅방 ID를 추출합니다.
        Long chatRoomId = extractChatRoomIdFromSession(session);

        // 해당 채팅방의 세션 리스트 get
        List<WebSocketSession> roomSessions = chatSessions.get(chatRoomId);

        if (roomSessions != null) {
            // 현재 세션을 해당 채팅방의 세션 리스트에서 제거
            roomSessions.remove(session);

            // 세션 리스트가 비었으면 맵에서 해당 채팅방의 세션 리스트 제거
            if (roomSessions.isEmpty()) {
                // 채킹방 세션 맵에서 해당 채팅방 제거
                chatSessions.remove(chatRoomId);
            }

        }
        log.info("WebSocket connection closed for chat room: {}", chatRoomId);
    }

    // URL 에서 채팅방 ID 추출
    private Long extractChatRoomIdFromSession(WebSocketSession session) {
        String uri = Objects.requireNonNull(session.getUri()).toString();
        String[] parts = uri.split("/");
        String roomIdString = parts[parts.length-1];
        return Long.parseLong(roomIdString);
    }
}
