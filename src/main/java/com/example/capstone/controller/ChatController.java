package com.example.capstone.controller;

import com.example.capstone.data.PostResponse;
import com.example.capstone.data.ServerErrorResponse;
import com.example.capstone.data.TokenResponse;
import com.example.capstone.dto.ChatDTO;
import com.example.capstone.dto.ChatRoomDTO;
import com.example.capstone.entity.ChatRoom;
import com.example.capstone.entity.Post;
import com.example.capstone.entity.UserEntity;
import com.example.capstone.jwt.TokenProvider;
import com.example.capstone.repository.PostRepository;
import com.example.capstone.repository.UserRepository;
import com.example.capstone.service.ChatService;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ChatService chatService;
    private final TokenProvider tokenProvider;
    private ObjectNode responseJson;

    // 채팅방 생성 API
    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody ChatDTO chatDTO, HttpServletRequest request) {

        try {
            responseJson = JsonNodeFactory.instance.objectNode();

            // 토큰 값 추출
            String token = request.getHeader("Authorization");
            token = token.replaceAll("Bearer ", "");

            // 토큰 검증
            if (!tokenProvider.validateToken(token)) {
                return TokenResponse.handleUnauthorizedRequest("유효하지 않은 토큰입니다.");
            }

            String userId = chatDTO.getApplicantId();
            Long pid = chatDTO.getExchangePostId();

            Optional<UserEntity> user = userRepository.findById(userId); // 신청자 정보
            Post post = postRepository.findByPid(pid); // 해당 재능교환 게시글

            log.info("user: {}", user);

            if (user.isPresent()) { // 가입된 신청자인 경우
                // 해당 재능교환 게시글이 있는지 체크
                if (post == null) { // 해당 게시글이 없는 경우
                    return PostResponse.notExistPost("없거나 삭제된 게시글입니다.");
                }

                // 채팅방을 생성하기 전에 채팅방이 존재하는지 먼저 확인
                // exchangePostId 와 applicantId 에 대한 신청자 uid 까지 똑같으면 같은 채팅방
                Long exchangePostId = chatDTO.getExchangePostId();
                String applicantId = chatDTO.getApplicantId();
                ChatRoom chatRoom = chatService.isExistChatRoom(exchangePostId, applicantId);
                if (chatRoom != null) { // 이미 채팅방이 존재하면
                    responseJson.put("message", "이미 채팅방이 존재합니다.");
                    responseJson.put("roomId", chatRoom.getRoomId());
                    responseJson.put("roomName", chatRoom.getRoomName());

                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseJson);

                } else { // 채팅방이 없으면 새로 생성
                    // 채팅방 생성
                    ChatRoom newChatRoom = chatService.createRoom(post.getUser(), user.get(), post);

                    responseJson.put("roomId", newChatRoom.getRoomId());
                    responseJson.put("roomName", newChatRoom.getRoomName());

                    return ResponseEntity.status(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseJson);
                }
            } else { // 회원가입된 사용자가 아닐 경우
                responseJson.put("message", "가입된 사용자가 아닙니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseJson);
            }
        } catch (Exception e) {
            return ServerErrorResponse.handleServerError("서버에 예기치 않은 오류가 발생했습니다." + e);
        }
    }

    // 채팅방 목록 조회 API
    @Transactional
    @GetMapping("/rooms/{postId}")
    public ResponseEntity<?> getARooms(@PathVariable Long postId, HttpServletRequest request) { // HttpServletRequest request 나중에 추가할 것
        try {
            // 토큰 값 추출
            String token = request.getHeader("Authorization");
            token = token.replaceAll("Bearer ", "");

            // 토큰 검증
            if (!tokenProvider.validateToken(token)) {
                return TokenResponse.handleUnauthorizedRequest("유효하지 않은 토큰입니다.");
            }

            // 현재 사용자가 작성한 게시글에 대한 채팅방만 가져오기
            // 게시글 기준으로 분류해서 체팅방 조회
            Post post = postRepository.findByPid(postId);
            if (post == null) {
                return PostResponse.notExistPost("없거나 삭제된 게시물입니다.");
            }

            List<ChatRoom> chatRooms = chatService.getRoomsByPostId(postId);
            List<ChatRoomDTO> chatRoomDTOList = ChatRoomDTO.toChatRoomDTOList(chatRooms);

            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(chatRoomDTOList);

        } catch (Exception e) {
            return ServerErrorResponse.handleServerError("서버에 예기치 않은 오류가 발생했습니다." + e);
        }
    }
}
