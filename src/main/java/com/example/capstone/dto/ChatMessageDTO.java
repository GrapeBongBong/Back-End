package com.example.capstone.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageDTO {

    public enum MessageType {
        ENTER, // 사용자가 처음 채팅방에 들어온 경우
        TALK // 이미 session 에 연결되어 채팅 중인 상태
    }

    private MessageType messageType;
    private Long roomId; // 메시지를 보낼 채팅방 id
    private String sender; // 보내는 사람의 닉네임
    private String message;
}
