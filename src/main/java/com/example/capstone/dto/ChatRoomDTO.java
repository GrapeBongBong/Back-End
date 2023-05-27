package com.example.capstone.dto;

import com.example.capstone.entity.ChatRoom;
import com.example.capstone.entity.ExchangePost;
import com.example.capstone.entity.Post;
import com.example.capstone.entity.UserEntity;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
public class ChatRoomDTO {
    // json 데이터를 받아 WebSocketHandler 에서 해당 데이터에 담긴 roomId 를 chatService 를 통해서 조회
    // 해당 id 의 채팅방을 찾아 json 데이터에 담긴 메시지를 해당 채팅방으로 보냄

    private Long postWriterUID;
    private Long pid;
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
        chatRoomDTO.setPostWriterUID(chatRoom.getPostWriter().getUid());
        chatRoomDTO.setPid(chatRoom.getExchangePost().getPid());
        chatRoomDTO.setRoomId(chatRoom.getRoomId());
        chatRoomDTO.setRoomName(chatRoom.getRoomName());
        chatRoomDTO.setDate(chatRoom.getDate());

        return chatRoomDTO;
    }

    public static List<ChatRoomDTO> toChatRoomDTOListByUser(List<ChatRoom> chatRooms, UserEntity user) {
        List<ChatRoomDTO> chatRoomDTOList = new ArrayList<>();

        for (ChatRoom chatRoom: chatRooms) {
            ChatRoomDTO chatRoomDTO = ChatRoomDTO.toChatRoomDTOByUser(chatRoom, user);
            chatRoomDTOList.add(chatRoomDTO);
        }

        return chatRoomDTOList;
    }

    private static ChatRoomDTO toChatRoomDTOByUser(ChatRoom chatRoom, UserEntity user) {
        ChatRoomDTO chatRoomDTO = new ChatRoomDTO();
        ExchangePost exchangePost = chatRoom.getExchangePost();
        chatRoomDTO.setPostWriterUID(exchangePost.getUser().getUid());
        chatRoomDTO.setPid(exchangePost.getPid());
        chatRoomDTO.setRoomId(chatRoom.getRoomId());
        // user 가 신청자라면, 채팅방 이름 = 게시글 제목 (게시글 작성자 닉네임)
        if (Objects.equals(user.getUid(), chatRoom.getApplicant().getUid())) {
            UserEntity postWriter = chatRoom.getPostWriter();
            chatRoomDTO.setRoomName(chatRoom.getExchangePost().getTitle() + " (" + postWriter.getNickName() + ")");
        } else if (Objects.equals(user.getUid(), chatRoom.getPostWriter().getUid())) {
            // user 가 게시글 작성자라면, 채팅방 이름 = 게시글 제목 (신청자 닉네임) >> 즉, DB 에 저장되어 있는 채팅방 이름 그대로
            chatRoomDTO.setRoomName(chatRoom.getRoomName());
        }
        chatRoomDTO.setDate(chatRoom.getDate());

        return chatRoomDTO;
    }
}
