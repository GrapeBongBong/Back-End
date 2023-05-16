package com.example.capstone.handler;

import com.example.capstone.dto.ChatMessageDTO;
import com.example.capstone.entity.ChatMessage;
import com.example.capstone.entity.ChatRoom;
import com.example.capstone.repository.ChatMessageRepository;
import com.example.capstone.repository.ChatRoomRepository;
import com.example.capstone.service.ChatService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.persistence.EntityManager;
import java.util.*;

@RequiredArgsConstructor
@Component
@Slf4j
public class ChatHandler extends TextWebSocketHandler { // Client 가 Send 할 수 있는 경로

    private final ObjectMapper objectMapper;
    private final ChatService chatService;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private ObjectNode responseJson;

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
            roomSession.sendMessage(new TextMessage(chatMessageDTO.getMessage()));
        }
    }

    // Client 접속 시 호출되는 메서드
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 클라이언트의 요청에서 채팅방 ID 추출
        Long chatRoomId = extractChatRoomIdFromSession(session);

        // 채팅방 ID 로 현재 채팅방이 있는지 확인
        ChatRoom chatRoom = chatRoomRepository.findChatRoomByRoomId(chatRoomId);
        if (chatRoom == null) {
            responseJson = objectMapper.createObjectNode();
            responseJson.put("message", "해당 채팅방이 존재하지 않습니다.");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(responseJson)));
        } else {
            // 채팅방에 해당하는 세션 리스트를 가져옴
            List<WebSocketSession> roomSessions = chatSessions.getOrDefault(chatRoomId, new ArrayList<>());

            // 해당 채팅방에 대한 세션에 최초 접속 시에만 DB 에서 메시지 조회해서 클라이언트로 전달
            if (roomSessions.isEmpty()) {
                // 해당 채팅방에 대해 DB 에 저장된 이전 채팅 메시지들 조회
                List<ChatMessage> chatMessageList = chatMessageRepository.getChatMessagesByChatRoom(chatRoom);
                List<ChatMessageDTO> chatMessageDTOList = new ArrayList<>();

                for (ChatMessage chatMessage: chatMessageList) {
                    ChatMessageDTO chatMessageDTO = ChatMessageDTO.toChatMessageDTO(chatMessage);
                    chatMessageDTOList.add(chatMessageDTO);
                    log.info("chatMessageDTO: {}", chatMessageDTO);
                }

                JsonNode chatMessages = objectMapper.convertValue(chatMessageDTOList, JsonNode.class);
                log.info("chatMessages: {}", chatMessages);

                for (WebSocketSession roomSession: roomSessions) {
                    roomSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(chatMessages)));
                }
            }

            // 현재 세션을 해당 채팅방의 세션 리스트에 추가
            roomSessions.add(session);

            // 업데이트된 세션 리스트를 맵에 다시 저장
            chatSessions.put(chatRoomId, roomSessions);
            log.info("WebSocket connection established for chat room: {}", chatRoomId);
        }
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
