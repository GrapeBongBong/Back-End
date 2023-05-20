package com.example.capstone.dto;

import com.example.capstone.entity.ChatMessage;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ChatMessageDTO {

    // 일대일이니까 대화 상태만 있어도 될 듯?
    /*public enum MessageType {
        ENTER, // 사용자가 처음 채팅방에 들어온 경우
        TALK, // 이미 session 에 연결되어 채팅 중인 상태
        LEAVE // 사용자가 채팅방을 나간 경우
    }*/

//    private MessageType messageType;
    private Long roomId; // 메시지를 보낼 채팅방 id
    private String senderId; // 보내는 사람의 아이디
    private String message;
}
