package com.example.capstone.service;

import com.example.capstone.dto.ChatMessageDTO;
import com.example.capstone.entity.*;
import com.example.capstone.repository.ChatMessageRepository;
import com.example.capstone.repository.ChatRoomRepository;
import com.example.capstone.repository.PostRepository;
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
    private final PostRepository postRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    public List<ChatRoom> getRoomsByPostId(Long postId) {
        ExchangePost exchangePost = (ExchangePost) postRepository.findByPid(postId);
        return chatRoomRepository.findChatRoomsByExchangePost(exchangePost);
    }

    public ChatRoom createRoom(UserEntity user1, UserEntity user2, Post post) {
        // user1 >> 게시글 작성자
        // user2 >> 해당 게시글에 신청한 사람

        ExchangePost exchangePost = (ExchangePost) post;
        ChatRoom chatRoom = new ChatRoom();
        String roomName = user2.getNickName() + " (" + user2.getId() + ")";
        chatRoom.setRoomName(roomName);
        chatRoom.setDate(formatDate(LocalDateTime.now()));
        chatRoom.setPostWriter(user1);
        chatRoom.setApplicant(user2);
        chatRoom.setExchangePost(exchangePost);

        chatRoomRepository.save(chatRoom);

        return chatRoom;
    }

    public ChatRoom isExistChatRoom(Long exchangePostId, String applicantId) {
        // exchangePostId 에 해당하는 게시글 찾기
        ExchangePost exchangePost = (ExchangePost) postRepository.findByPid(exchangePostId);
        Optional<UserEntity> applicant = userRepository.findById(applicantId);
        if (applicant.isPresent()) {
            return chatRoomRepository.findChatRoomByExchangePostAndApplicant(exchangePost, applicant.get());
        } else {
            return null;
        }
    }

    public Long saveMessage(ChatMessageDTO chatMessageDTO, Long chatRoomId) { // 저장 후 해당 채팅 메시지 아이디 리턴
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
        return chatMessage.getId();
    }

    public String formatDate(LocalDateTime localDateTime) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedTime = localDateTime.format(dateTimeFormatter);
        return formattedTime;
    }
}
