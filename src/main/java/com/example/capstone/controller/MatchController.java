package com.example.capstone.controller;

import com.example.capstone.data.ServerErrorResponse;
import com.example.capstone.data.TokenResponse;
import com.example.capstone.entity.ChatRoom;
import com.example.capstone.entity.ExchangePost;
import com.example.capstone.entity.Match;
import com.example.capstone.entity.Post;
import com.example.capstone.jwt.TokenProvider;
import com.example.capstone.repository.ChatRoomRepository;
import com.example.capstone.repository.MatchRepository;
import com.example.capstone.repository.PostRepository;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Api(tags = {"매칭 API"})
@RestController
@RequiredArgsConstructor
@Slf4j
public class MatchController {

    private final TokenProvider tokenProvider;
    private final PostRepository postRepository;
    private final MatchRepository matchRepository;
    private final ChatRoomRepository chatRoomRepository;
    private ObjectNode responseJson;

    // 매칭 진행 API
    @ApiOperation(value = "매칭 진행", notes = "재능 교환 게시물의 매칭을 진행합니다.", response = ResponseEntity.class)
    @PostMapping("/match/{postId}")
    @Transactional
    public ResponseEntity<?> processMatch(@PathVariable Long postId, HttpServletRequest request) {
        responseJson = JsonNodeFactory.instance.objectNode();

        try {
            log.info("Entering processMatch method"); // 메서드 진입 로그

            // 토큰 값 추출
            String token = request.getHeader("Authorization");
            token = token.replaceAll("Bearer ", "");

            // 토큰 검증
            if (!tokenProvider.validateToken(token)) {
                return TokenResponse.handleUnauthorizedRequest("유효하지 않은 토큰입니다.");
            }

            // 게시물 조회
            log.info("게시물 ID: {}", postId);
            Optional<Post> exchangePostOptional = postRepository.findById(postId);
            if (exchangePostOptional.isPresent()) {
                ExchangePost exchangePost = (ExchangePost) exchangePostOptional.get();

                // 이미 매칭이 진행 중인 경우
                if (!exchangePost.getStatus()) {
                    responseJson.put("message", "이미 매칭이 진행 중인 게시물입니다.");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseJson);
                }

                // 채팅방에서 작성자와 신청자 정보 알아오기
                Optional<ChatRoom> chatRoomOptional = chatRoomRepository.findRoomIdByExchangePost(exchangePost);
                log.info("Chat room found: {}", chatRoomOptional.isPresent());
                if (chatRoomOptional.isPresent()) {
                    log.info("Chat room information retrieved");
                    ChatRoom chatRoom = chatRoomOptional.get();

                    if (chatRoom.getWriterId() == null || chatRoom.getApplicant() == null) {
                        responseJson.put("message", "채팅방에 작성자와 신청자 정보가 없습니다.");
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(responseJson);
                    }

                    Long chatRoomId = chatRoom.getRoomId();
                    Long writerId = chatRoom.getWriterId();
                    Long applicantId = chatRoom.getApplicantId();

                    System.out.println("채팅방 아이디: " + chatRoomId);
                    System.out.println("작성자: " + writerId);
                    System.out.println("신청자: " + applicantId);

                    // 매칭 진행
                    Match match = new Match();
                    log.info("Creating match object");
                    match.setExchangePost(exchangePost);
                    match.setChatRoomId(chatRoomId);
                    match.setWriterId(writerId);
                    match.setApplicantId(applicantId);

                    matchRepository.save(match);

                    // 재능 교환 게시물의 상태를 0으로 변경
                    log.info("Updating exchange post status");
                    exchangePost.setStatus(false);
                    postRepository.save(exchangePost);

                    // 매칭 정보를 응답에 포함하여 반환
                    responseJson.put("message", "매칭이 성공적으로 진행되었습니다.");

                    return ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseJson);
                } else {
                    responseJson.put("message", "매칭을 진행할 게시물을 찾을 수 없습니다.");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseJson);
                }
            } else {
                responseJson.put("message", "매칭을 진행할 게시물을 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseJson);
            }
        } catch (Exception e) {
            return ServerErrorResponse.handleServerError("서버에 예기치 않은 오류가 발생했습니다." + e);
        }
    }
}
