package com.example.capstone.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class ChatRoomDTO {
    private Long exchangePostId; // 재능교환 게시글 pid
    private String participantId; // 참가자 id
//    private Set<WebSocketSession> sessions = new HashSet<>();

    /*public void handlerAction(WebSocketSession session, ChatMessageDTO chatMessageDTO, ChatService chatService) {
        if (chatMessageDTO.getMessageType().equals(ChatMessageDTO.MessageType.ENTER)) {
            sessions.add(session);
            chatMessageDTO.setMessage(chatMessageDTO.getSender() + "님이 입장했습니다.");
        }
        sendMessage(chatMessageDTO, chatService);
    }

    private <T> void sendMessage(T message, ChatService chatService) {
        sessions.parallelStream()
                .forEach(sesssion -> chatService.sendMessage(sesssion, message));
    }*/
}
