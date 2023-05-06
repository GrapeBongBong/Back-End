package com.example.capstone.handler;

import com.example.capstone.dto.ChatMessageDTO;
import com.example.capstone.dto.ChatRoomDTO;
import com.example.capstone.entity.ChatRoom;
import com.example.capstone.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@RequiredArgsConstructor
@Component
@Slf4j
public class WebSocketHandler extends TextWebSocketHandler { // Client 가 Send 할 수 있는 경로
    private final ObjectMapper objectMapper;
    private final ChatService chatService;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("payload {}", payload);

        ChatMessageDTO chatMessageDTO = objectMapper.readValue(payload, ChatMessageDTO.class); // 보내온 json 데이터를 chatMessage.class 에 맞게 파싱
        log.info("session {}", chatMessageDTO.toString());

        ChatRoom chatRoom = chatService.findRoomById(chatMessageDTO.getRoomId());
        ChatRoomDTO chatRoomDTO = ChatRoomDTO.toChatRoomDTO(chatRoom);
        log.info("room {}", chatRoomDTO);

        chatRoomDTO.handlerActions(session, chatMessageDTO, chatService);
    }
}
