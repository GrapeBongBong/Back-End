package com.example.capstone.dto;

import com.example.capstone.entity.ChatMessage;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ChatMessageResponseDTO {
    private Long chatMessageId;
    private Long roomId; // 메시지를 보낼 채팅방 id
    private String senderId; // 보내는 사람의 아이디
    private String message;

    public static ChatMessageResponseDTO toChatMessageResponseDTO(ChatMessage chatMessage) {
        ChatMessageResponseDTO chatMessageResponseDTO = new ChatMessageResponseDTO();
        chatMessageResponseDTO.setChatMessageId(chatMessage.getId());
        chatMessageResponseDTO.setRoomId(chatMessage.getChatRoom().getRoomId());
        chatMessageResponseDTO.setSenderId(chatMessage.getSender().getId());
        chatMessageResponseDTO.setMessage(chatMessage.getMessage());

        return chatMessageResponseDTO;
    }
}
