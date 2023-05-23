package com.example.capstone.controller;

import com.example.capstone.data.ServerErrorResponse;
import com.example.capstone.data.TokenResponse;
import com.example.capstone.dto.CommentDTO;
import com.example.capstone.dto.CommentRequestDTO;
import com.example.capstone.entity.Comment;
import com.example.capstone.entity.Post;
import com.example.capstone.entity.UserEntity;
import com.example.capstone.jwt.TokenProvider;
import com.example.capstone.repository.CommentRepository;
import com.example.capstone.repository.PostRepository;
import com.example.capstone.repository.UserRepository;
import com.example.capstone.service.CommentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Api(tags = {"댓글 API"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/{postId}")
public class CommentController {
    private final CommentService commentService;
    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    //댓글 등록 API
    @PostMapping("/comment")
    public ResponseEntity<?> createComment(@PathVariable Long postId, @Valid @RequestBody CommentRequestDTO commentRequestDTO, BindingResult bindingResult, HttpServletRequest request) {
        JsonNodeFactory jsonFactory = JsonNodeFactory.instance;
        ObjectNode responseJson = jsonFactory.objectNode();

        // 필수정보 체크
        if (bindingResult.hasErrors()) {
            responseJson.put("message", "필수 정보가 없습니다.");
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(responseJson);
        }

        try {
            // 토큰 값 추출
            String token = request.getHeader("Authorization");
            token = token.replaceAll("Bearer ", "");

            // 토큰 검증
            if (!tokenProvider.validateToken(token)) {
                return TokenResponse.handleUnauthorizedRequest("유효하지 않은 토큰입니다.");
            }
            System.out.println("실행2");
            // 헤더에 첨부되어 있는 token 에서 로그인 된 사용자 정보 받아옴
            Authentication authentication = tokenProvider.getAuthentication(token);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            String id = userDetails.getUsername(); // UserDetails 객체에서 사용자 아이디를 가져옴
            System.out.println(id);

            // UserEntity를 사용자 아이디를 기반으로 조회
            Optional<UserEntity> loggedInUserEntity = userRepository.findById(id); // 사용자 아이디를 기반으로 사용자 조회
            UserEntity userEntity = null;

            if (loggedInUserEntity.isPresent()) {
                userEntity = loggedInUserEntity.get();
                Long uid = userEntity.getUid(); // 가져온 UserEntity 객체에서 Uid를 가져옴
                System.out.println("User = " + userEntity);
                System.out.println("uid = " + uid);

                CommentDTO createdComment = commentService.createComment(commentRequestDTO, postId, uid);
                responseJson.put("message", "댓글이 성공적으로 등록되었습니다.");
                //responseJson.put("createdComment", String.valueOf(createdComment));
                System.out.println(createdComment);

                return ResponseEntity.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseJson);

                } else {
                    responseJson.put("message", "로그인이 필요합니다");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseJson);
                }
        } catch (Exception e) {
            return ServerErrorResponse.handleServerError("서버에 예기치 않은 오류가 발생했습니다." + e);
        }
    }

    //댓글 수정
    @PutMapping("/comment/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable Long postId, @PathVariable Long commentId, @Valid @RequestBody CommentRequestDTO commentRequestDTO, BindingResult bindingResult, HttpServletRequest request) {
        JsonNodeFactory jsonFactory = JsonNodeFactory.instance;
        ObjectNode responseJson = jsonFactory.objectNode();

        // 필수정보 체크
        if (bindingResult.hasErrors()) {
            responseJson.put("message", "필수 정보가 없습니다.");
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(responseJson);
        }

        try {
            // 토큰 값 추출
            String token = request.getHeader("Authorization");
            token = token.replaceAll("Bearer ", "");

            // 토큰 검증
            if (!tokenProvider.validateToken(token)) {
                return TokenResponse.handleUnauthorizedRequest("유효하지 않은 토큰입니다.");
            }

            // 헤더에 첨부되어 있는 token 에서 로그인 된 사용자 정보 받아옴
            Authentication authentication = tokenProvider.getAuthentication(token);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            String id = userDetails.getUsername(); // UserDetails 객체에서 사용자 아이디를 가져옴

            // UserEntity를 사용자 아이디를 기반으로 조회
            Optional<UserEntity> loggedInUserEntity = userRepository.findById(id); // 사용자 아이디를 기반으로 사용자 조회
            UserEntity userEntity = null;

            if (loggedInUserEntity.isPresent()) {
                userEntity = loggedInUserEntity.get();
                Long uid = userEntity.getUid(); // 가져온 UserEntity 객체에서 Uid를 가져옴

                // 해당 댓글을 작성한 사용자와 로그인한 사용자가 같은지 확인
                Optional<Comment> comment = commentRepository.findById(commentId);
                if (comment.isPresent() && comment.get().getUser().getUid().equals(uid)) {
                    CommentDTO updatedComment = commentService.updateComment(commentId, commentRequestDTO);
                    responseJson.put("message", "댓글이 성공적으로 수정되었습니다.");
                    //responseJson.put("updatedComment", String.valueOf(updatedComment));

                    return ResponseEntity.status(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseJson);
                } else {
                    responseJson.put("message", "해당 댓글을 수정할 권한이 없습니다.");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseJson);
                }

            } else {
                responseJson.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseJson);
            }
        } catch (Exception e) {
            return ServerErrorResponse.handleServerError("서버에 예기치 않은 오류가 발생했습니다." + e);
        }
    }

    //댓글 삭제
    @DeleteMapping("/comment/delete/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId, HttpServletRequest request) {
        JsonNodeFactory jsonFactory = JsonNodeFactory.instance;
        ObjectNode responseJson = jsonFactory.objectNode();

        try {
            // 토큰 값 추출
            String token = request.getHeader("Authorization");
            token = token.replaceAll("Bearer ", "");

            // 토큰 검증
            if (!tokenProvider.validateToken(token)) {
                return TokenResponse.handleUnauthorizedRequest("유효하지 않은 토큰입니다.");
            }

            // 헤더에 첨부되어 있는 token 에서 로그인 된 사용자 정보 받아옴
            Authentication authentication = tokenProvider.getAuthentication(token);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            String id = userDetails.getUsername(); // UserDetails 객체에서 사용자 아이디를 가져옴

            // UserEntity를 사용자 아이디를 기반으로 조회
            Optional<UserEntity> loggedInUserEntity = userRepository.findById(id); // 사용자 아이디를 기반으로 사용자 조회

            if (loggedInUserEntity.isPresent()) {
                UserEntity userEntity = loggedInUserEntity.get();
                Long uid = userEntity.getUid(); // 가져온 UserEntity 객체에서 Uid를 가져옴

                Optional<Comment> comment = commentRepository.findById(commentId);
                if (comment.isPresent()) {
                    Comment targetComment = comment.get();
                    if (targetComment.getUser().getUid().equals(uid)) {
                        commentService.deleteComment(commentId);
                        responseJson.put("message", "댓글이 성공적으로 삭제되었습니다.");
                        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(responseJson);
                    } else {
                        responseJson.put("message", "해당 댓글의 작성자가 아닙니다.");
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).contentType(MediaType.APPLICATION_JSON).body(responseJson);
                    }
                } else {
                    responseJson.put("message", "존재하지 않는 댓글입니다.");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body(responseJson);

                }
            } else {
                responseJson.put("message", "로그인이 필요합니다");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseJson);
            }
        } catch (Exception e) {
            return ServerErrorResponse.handleServerError("서버에 예기치 않은 오류가 발생했습니다." + e);
        }
    }

    //댓글 조회
    @GetMapping("/comments")
    public ResponseEntity<?> getComments(@PathVariable Long postId, HttpServletRequest request) {
        JsonNodeFactory jsonFactory = JsonNodeFactory.instance;
        ObjectNode responseJson = jsonFactory.objectNode();

        try {
            // 토큰 값 추출
            String token = request.getHeader("Authorization");
            token = token.replaceAll("Bearer ", "");

            // 토큰 검증
            if (!tokenProvider.validateToken(token)) {
                return TokenResponse.handleUnauthorizedRequest("유효하지 않은 토큰입니다.");
            }

            List<CommentDTO> commentDTOs = commentService.getCommentsByPostId(postId);

            // ObjectMapper를 사용하여 pretty-printing된 JSON 문자열 생성
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT); // 들여쓰기 설정

            responseJson.putPOJO("comments", commentDTOs);
            //return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(mapper.writeValueAsString(responseJson));

            //responseJson.put("comments", commentDTOs);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(responseJson);
        } catch (Exception e) {
            return ServerErrorResponse.handleServerError("서버에 예기치 않은 오류가 발생했습니다." + e);
        }
    }
}

