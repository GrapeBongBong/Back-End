package com.example.capstone.service;

import com.example.capstone.dto.ChatMessageDTO;
import com.example.capstone.entity.ChatMessage;
import com.example.capstone.entity.ChatRoom;
import com.example.capstone.entity.ExchangePost;
import com.example.capstone.entity.UserEntity;
import com.example.capstone.repository.ChatMessageRepository;
import com.example.capstone.repository.ChatRoomRepository;
import com.example.capstone.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class ChatService {

    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    public List<ChatRoom> getAllRooms() {
        return chatRoomRepository.findAll();
    }

    public ChatRoom findRoomById(Long roomId) { return chatRoomRepository.findChatRoomByRoomId(roomId); }

    public ChatRoom createRoom(UserEntity user1, UserEntity user2, ExchangePost exchangePost) {
        // user1 >> 게시글 작성자
        // user2 >> 해당 게시글에 신청한 사람

        ChatRoom chatRoom = new ChatRoom();
        String roomName = user2.getNickName() + " (" + user2.getNickName() + ")";
        chatRoom.setRoomName(roomName);
        chatRoom.setDate(formatDate(LocalDateTime.now()));
        chatRoom.setPostWriter(user1);
        chatRoom.setApplicant(user2);
        chatRoom.setExchangePost(exchangePost);

        chatRoomRepository.save(chatRoom);

        return chatRoom;
    }

    public void saveMessage(ChatMessageDTO chatMessageDTO, Long chatRoomId) {
        String senderId = chatMessageDTO.getSenderId();
        Optional<UserEntity> loggedInUser = userRepository.findById(senderId);
        ChatRoom chatRoom = chatRoomRepository.findChatRoomByRoomId(chatRoomId);

        UserEntity user = new UserEntity();
        if (loggedInUser.isPresent()) {
            user = loggedInUser.get();
        }

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMessage(chatMessageDTO.getMessage());
        chatMessage.setSender(user);
        chatMessage.setChatRoom(chatRoom);
        chatMessage.setDate(formatDate(LocalDateTime.now()));

        chatMessageRepository.save(chatMessage);
    }

    public String formatDate(LocalDateTime localDateTime) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedTime = localDateTime.format(dateTimeFormatter);
        return formattedTime;
    }
}
