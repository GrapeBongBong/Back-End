package com.example.capstone.controller;

import com.example.capstone.dto.ChatDTO;
import com.example.capstone.dto.ChatRoomDTO;
import com.example.capstone.entity.ChatMessage;
import com.example.capstone.entity.ChatRoom;
import com.example.capstone.entity.ExchangePost;
import com.example.capstone.entity.UserEntity;
import com.example.capstone.handler.WebSocketSessionManager;
import com.example.capstone.jwt.TokenProvider;
import com.example.capstone.repository.ChatRoomRepository;
import com.example.capstone.repository.PostRepository;
import com.example.capstone.repository.UserRepository;
import com.example.capstone.service.ChatService;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatService chatService;
    private final TokenProvider tokenProvider;
    private ObjectNode responseJson;

    // 채팅방 생성 API
    @PostMapping
    public ChatRoomDTO createRoom(@RequestBody ChatDTO chatDTO, HttpServletRequest request) {

        try {
            responseJson = JsonNodeFactory.instance.objectNode();

            // 토큰 값 추출
            String token = request.getHeader("Authorization");
            token = token.replaceAll("Bearer ", "");

            // 토큰 검증
            if (!tokenProvider.validateToken(token)) {
                responseJson.put("message", "유효하지 않은 토큰입니다.");

                /*return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseJson);*/
            }

            String userId = chatDTO.getApplicantId();
            Long pid = chatDTO.getExchangePostId();

            Optional<UserEntity> user = userRepository.findById(userId); // 신청자 정보
            ExchangePost exchangePost = (ExchangePost) postRepository.findByPid(pid); // 해당 재능교환 게시글

            if (user.isPresent()) {
                ChatRoom chatRoom = chatService.createRoom(exchangePost.getUser(), user.get(), exchangePost);
                WebSocketSessionManager sessionManager = WebSocketSessionManager.getInstance();
                sessionManager.addSession(chatRoom.getRoomId(), session);

//                responseJson.put("roomId", roomId);

                /*return ResponseEntity.status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseJson);*/
//                return chatService.createRoom(exchangePost.getUser(), user.get(), exchangePost);
            } else {
                /*responseJson.put("message", "가입된 사용자가 아닙니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseJson);*/
                return null;
            }
        } catch (Exception e) {
            responseJson = JsonNodeFactory.instance.objectNode();
            responseJson.put("message", "서버에 예기치 않은 오류가 발생했습니다." + e);

//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .body(responseJson);
            return null;
        }
    }

    // 채팅방 목록 조회 API
    @Transactional
    @GetMapping("/rooms")
    public ResponseEntity<?> getAllRooms() { // HttpServletRequest request 나중에 추가할 것
        try {
            responseJson = JsonNodeFactory.instance.objectNode();
            List<ChatRoom> chatRooms = chatService.getAllRooms();

            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(chatRooms);

        } catch (Exception e) {
            responseJson = JsonNodeFactory.instance.objectNode();
            responseJson.put("message", "서버에 예기치 않은 오류가 발생했습니다." + e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(responseJson);
        }
    }

    /*// 채팅 메시지 보내는 API
    @PostMapping("/{roomId}")
    public ResponseEntity<?> sendMessage(@PathVariable("roomId") Long roomId, @RequestBody ChatMessage chatMessage, HttpServletRequest request) {

        try {
            responseJson = JsonNodeFactory.instance.objectNode();

            // 토큰 값 추출
            String token = request.getHeader("Authorization");
            token = token.replaceAll("Bearer ", "");

            // 토큰 검증
            if (!tokenProvider.validateToken(token)) {
                responseJson.put("message", "유효하지 않은 토큰입니다.");

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseJson);
            }

            // 헤더에 첨부되어 있는 token 에서 로그인된 사용자 정보 받아옴
            Authentication authentication = tokenProvider.getAuthentication(token);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            String userId = userDetails.getUsername(); // UserDetails 객체에서 사용자 아이디를 가져옴

            ChatRoom chatRoom = chatRoomRepository.findChatRoomByRoomId(roomId);
            Optional<UserEntity> userEntity = userRepository.findById(userId);

            // 현재 로그인된 사용자 (요청한 사용자) 가 채팅방 참가자인지 확인
            if (userEntity.isPresent()) {
                if (chatRoom.getApplicant().getId().equals(userId)) { // 사용자가 신청자 역할인 경우
                    chatMessage.setSender(chatRoom.getApplicant()); // 신청자를 센더로
                } else if (chatRoom.getPostWriter().getId().equals(userId)) { // 사용자가 게시글 작성자인 경우
                    chatMessage.setSender(chatRoom.getPostWriter()); // 게시글 작성자를 센더로
                }
            }

            chatMessage.setDate(LocalDateTime.now());
            chatMessage.setChatRoom(chatRoom);

            responseJson = JsonNodeFactory.instance.objectNode();
            responseJson.put("message", "채팅 메시지가 전송되었습니다.");

            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(responseJson);

        } catch (Exception e) {
            responseJson = JsonNodeFactory.instance.objectNode();
            responseJson.put("message", "서버에 예기치 않은 오류가 발생했습니다." + e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(responseJson);
        }

    }*/
}
