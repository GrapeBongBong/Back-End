package com.example.capstone.dto;

import com.example.capstone.entity.ChatRoom;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Getter
@Setter
@ToString
public class ChatRoomDTO {
    // json 데이터를 받아 WebSocketHandler 에서 해당 데이터에 담긴 roomId 를 chatService 를 통해서 조회
    // 해당 id 의 채팅방을 찾아 json 데이터에 담긴 메시지를 해당 채팅방으로 보냄

    private Long roomId;
    private String roomName;
    private String date; // 채팅방 생성날짜

    public static List<ChatRoomDTO> toChatRoomDTOList(List<ChatRoom> chatRooms) {
        List<ChatRoomDTO> chatRoomDTOList = new ArrayList<>();

        for (ChatRoom chatRoom: chatRooms) {
            ChatRoomDTO chatRoomDTO = ChatRoomDTO.toChatRoomDTO(chatRoom);
            chatRoomDTOList.add(chatRoomDTO);
        }

        return chatRoomDTOList;
    }

    private static ChatRoomDTO toChatRoomDTO(ChatRoom chatRoom) {
        ChatRoomDTO chatRoomDTO = new ChatRoomDTO();
        chatRoomDTO.setRoomId(chatRoom.getRoomId());
        chatRoomDTO.setRoomName(chatRoom.getRoomName());
        chatRoomDTO.setDate(chatRoom.getDate());

        return chatRoomDTO;
    }
}
