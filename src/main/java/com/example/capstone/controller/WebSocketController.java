package com.example.capstone.controller;

import com.example.capstone.dto.ChatMessageDTO;
import com.example.capstone.dto.ChatRoomDTO;
import com.example.capstone.entity.ChatMessage;
import com.example.capstone.entity.ChatRoom;
import com.example.capstone.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@RequiredArgsConstructor
@Component
public class WebSocketController extends TextWebSocketHandler { // Client 가 Send 할 수 있는 경로
    private final ObjectMapper objectMapper;
    private final ChatService chatService;
//    private final SimpMessagingTemplate template; //특정 Broker 로 메세지 전달

    @MessageMapping("/chat/{postId}")
    @SendTo("/chat/messages/{postId}")
    public void send(ChatMessageDTO chatMessageDTO) {
        /*
            1. @MessageMapping 을 통해 WebSocket 으로 들어오는 메시지 발행을 처리
            2. Client 에서는 prefix 붙여서 "/topic/chat/{postId}" 로 발행 요청하면
            3. Controller 가 해당 메시지를 받아 처리.
            4. 메시지가 발행되면 "/queue/chat/room/{roomId}" 로 메시지 전송됨
         */
//        template.convertAndSend("/queue/chat/room" + chatMessageDTO.getRoomId(), chatMessageDTO);
    }

//    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        String payload = message.getPayload();
//        ChatMessageDTO chatMessageDTO = objectMapper.readValue(payload, ChatMessageDTO.class); // 보내온 json 데이터를 chatMessage.class 에 맞게 파싱
//
//        TextMessage textMessage = new TextMessage("hello");
//        session.sendMessage(textMessage);
//
//        /*ChatRoom chatRoom = chatService.findRoomById(chatMessageDTO.getRoomId());
//        ChatRoomDTO chatRoomDTO = ChatRoom.toChatRoomDTO(chatRoom);
//        chatRoomDTO.handlerAction(session, chatMessageDTO, chatService);*/
//
//        // handlerAction
//        // 이 참여자가 현재 이미 채팅방에 접속된 상태인지, 아니면 이미 채팅에 참여해있는 상태인지 판별
//        // 채팅방에 처음 참여하는 경우, session 연결 + 메시지 보냄
//        // 이미 참여해있는 경우, 메시지를 해당 채팅방에 보냄
//    }
}
