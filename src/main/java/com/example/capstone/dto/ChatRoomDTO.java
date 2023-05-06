package com.example.capstone.dto;

import com.example.capstone.entity.ChatRoom;
import com.example.capstone.service.ChatService;
import lombok.*;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashSet;
import java.util.Set;

@Data
@Getter
@Setter
@ToString
public class ChatRoomDTO {
    // json 데이터를 받아 WebSocketHandler 에서 해당 데이터에 담긴 roomId 를 chatService 를 통해서 조회
    // 해당 id 의 채팅방을 찾아 json 데이터에 담긴 메시지를 해당 채팅방으로 보냄

    private Long roomId;
    private String roomName;
    private Set<WebSocketSession> sessions = new HashSet<>();

    @Builder
    public ChatRoomDTO(Long roomId, String roomName) {
        this.roomId = roomId;
        this.roomName = roomName;
    }

    // handlerAction
    // 이 참여자가 현재 이미 채팅방에 접속된 상태인지, 아니면 이미 채팅에 참여해있는 상태인지 판별
    // 채팅방에 처음 참여하는 경우, session 연결 + 메시지 보냄
    // 이미 참여해있는 경우, 메시지를 해당 채팅방에 보냄

    public void handlerActions(WebSocketSession session, ChatMessageDTO chatMessageDTO, ChatService chatService) {
        if (chatMessageDTO.getMessageType().equals(ChatMessageDTO.MessageType.ENTER)) {
            sessions.add(session);
            chatMessageDTO.setMessage(chatMessageDTO.getSender() + "님이 입장했습니다.");
        } else if (chatMessageDTO.getMessageType().equals(ChatMessageDTO.MessageType.TALK)) {
            chatMessageDTO.setMessage(chatMessageDTO.getMessage());
        }
        sendMessage(chatMessageDTO, chatService);
    }

    private <T> void sendMessage(T message, ChatService chatService) {
        sessions.parallelStream()
                .forEach(session -> chatService.sendMessage(session, message));
    }

    public static ChatRoomDTO toChatRoomDTO(ChatRoom chatRoom) {
        Long roomId = chatRoom.getRoomId();
        String roomName = chatRoom.getRoomName();

        return new ChatRoomDTO(roomId, roomName);
    }
}
