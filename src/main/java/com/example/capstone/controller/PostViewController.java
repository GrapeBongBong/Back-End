package com.example.capstone.controller;

import com.example.capstone.data.ServerErrorResponse;
import com.example.capstone.data.TokenResponse;
import com.example.capstone.dto.AnonymousPostDTO;
import com.example.capstone.dto.ChatRoomDTO;
import com.example.capstone.dto.ExchangePostDTO;
import com.example.capstone.entity.*;
import com.example.capstone.jwt.TokenProvider;
import com.example.capstone.repository.ChatRoomRepository;
import com.example.capstone.repository.PostRepository;
import com.example.capstone.repository.UserRepository;
import com.example.capstone.service.LikePostService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Api(tags = {"프로필 내 본인 게시물 조회 API"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/profile")
@Slf4j
public class PostViewController {

    private final LikePostService likePostService;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final TokenProvider tokenProvider;
    private ObjectNode responseJson;

    // 사용자가 작성한 재능 교환 게시물 조회 API
    @GetMapping("/exchange")
    @Transactional
    public ResponseEntity<?> getExchangePosts(HttpServletRequest request) {
        try {
            responseJson = JsonNodeFactory.instance.objectNode();

            // 토큰 값 추출
            String token = request.getHeader("Authorization");
            token = token.replaceAll("Bearer ", "");

            // 토큰 검증
            if (!tokenProvider.validateToken(token)) {
                return TokenResponse.handleUnauthorizedRequest("유효하지 않은 토큰입니다.");
            } else {
                // 사용자 정보 가져오기
                Optional<UserEntity> user = TokenResponse.getLoggedInUser(tokenProvider, token, userRepository);
                if (user.isEmpty()) {
                    responseJson.put("message", "가입된 사용자자 아닙니다.");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseJson);
                }

                // 사용자 아이디 가져오기
                String userId = user.get().getId();

                // 사용자 이름, 교환 게시물 가져오기
                List<Post> exchangePostList = postRepository.findByUserIdAndPostType(userId, PostType.T);

                // 현재 로그인된 사용자가 해당 게시글에 좋아요를 눌렀는지 체크
                List<Boolean> isLikedList = likePostService.getIsLiked(user.get(), exchangePostList);
                List<ExchangePostDTO> exchangePostDTOList = ExchangePostDTO.toExchangePostDTOList(exchangePostList, isLikedList);

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode exchangePosts = objectMapper.convertValue(exchangePostDTOList, JsonNode.class);

                return ResponseEntity.status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(exchangePosts);
            }
        } catch (Exception e) {
            return ServerErrorResponse.handleServerError("서버에 예기치 않은 오류가 발생했습니다." + e);
        }
    }

    // 사용자가 작성한 익명 게시물 조회 API
    @GetMapping("/anonymous")
    @Transactional
    public ResponseEntity<?> getAnonymousPosts(HttpServletRequest request) {
        try {
            // 토큰 값 추출
            String token = request.getHeader("Authorization");
            token = token.replaceAll("Bearer ", "");

            // 토큰 검증
            if (!tokenProvider.validateToken(token)) {
                return TokenResponse.handleUnauthorizedRequest("유효하지 않은 토큰입니다.");
            } else {
                // 사용자 정보 가져오기
                Optional<UserEntity> user = TokenResponse.getLoggedInUser(tokenProvider, token, userRepository);
                if (user.isEmpty()) {
                    responseJson.put("message", "가입된 사용자자 아닙니다.");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseJson);
                }

                // 사용자 아이디 가져오기
                String userId = user.get().getId();

                // 사용자 이름, 교환 게시물 가져오기
                List<Post> anonymousPostList = postRepository.findByUserIdAndPostType(userId, PostType.A);
                // 현재 로그인된 사용자가 해당 게시글에 좋아요를 눌렀는지 체크
                List<Boolean> isLikedList = likePostService.getIsLiked(user.get(), anonymousPostList);
                List<AnonymousPostDTO> anonymousPostDTOList = AnonymousPostDTO.toAnonymousPostDTOList(anonymousPostList, isLikedList);

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode anonymousPosts = objectMapper.convertValue(anonymousPostDTOList, JsonNode.class);

                return ResponseEntity.status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(anonymousPosts);
            }
        } catch (Exception e) {
            return ServerErrorResponse.handleServerError("서버에 예기치 않은 오류가 발생했습니다." + e);
        }
    }

    // 사용자가 포함된 채팅방 목록 조회 API
    @Transactional
    @GetMapping("/chatRoom")
    public ResponseEntity<?> getUserChatRoom(HttpServletRequest request) {
        try {
            // 토큰 값 추출
            String token = request.getHeader("Authorization");
            token = token.replaceAll("Bearer ", "");

            // 토큰 검증
            if (!tokenProvider.validateToken(token)) {
                return TokenResponse.handleUnauthorizedRequest("유효하지 않은 토큰입니다.");
            } else {
                // 토큰에서 로그인된 사용자 정보 가져오기
                Authentication authentication = tokenProvider.getAuthentication(token);
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();

                // 사용자 이름 가져오기
                String userId = userDetails.getUsername();
                log.info("userId {}", userId);

                Optional<UserEntity> user = userRepository.findById(userId);
                if (user.isPresent()) {
                    // 현재 유저가 신청자로 있는 채팅방과 게시글 작성자로 있는 채팅방을 모두 조회
                    UserEntity userEntity = user.get();
                    List<ChatRoom> chatRoomList = chatRoomRepository.findChatRoomsByApplicantOrPostWriter(userEntity, userEntity);
                    List<ChatRoomDTO> chatRoomDTOList = ChatRoomDTO.toChatRoomDTOListByUser(chatRoomList, userEntity);

                    return ResponseEntity.status(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(chatRoomDTOList);
                } else {
                    ObjectNode responseJson = JsonNodeFactory.instance.objectNode();
                    responseJson.put("message", "가입된 사용자가 아닙니다.");

                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseJson);
                }

            }
        } catch (Exception e) {
            return ServerErrorResponse.handleServerError("서버에 예기치 않은 오류가 발생했습니다." + e);
        }
    }

}
