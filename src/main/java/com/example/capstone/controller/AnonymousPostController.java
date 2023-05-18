package com.example.capstone.controller;

import com.example.capstone.data.PostResponse;
import com.example.capstone.data.ServerErrorResponse;
import com.example.capstone.data.TokenResponse;
import com.example.capstone.dto.AnonymousPostDTO;
import com.example.capstone.entity.AnonymousPost;
import com.example.capstone.entity.Post;
import com.example.capstone.entity.PostType;
import com.example.capstone.entity.UserEntity;
import com.example.capstone.jwt.TokenProvider;
import com.example.capstone.repository.PostRepository;
import com.example.capstone.repository.UserRepository;
import com.example.capstone.service.PostService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/anonymous")
public class AnonymousPostController {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final TokenProvider tokenProvider;
    private final PostService postService;
    private ObjectNode responseJson;

    // 게시물 등록 API
    @PostMapping(value = "/post", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> createPost(@RequestPart AnonymousPostDTO anonymousPostDTO,
                                        @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles, BindingResult bindingResult, HttpServletRequest request) {
        responseJson = JsonNodeFactory.instance.objectNode();

        // 필수정보 체크
        if (bindingResult.hasErrors()) {
            responseJson.put("message", "필수 정보가 없습니다.");

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(responseJson);
        }

        try {
            // 토큰 값 추출
            String token = request.getHeader("Authorization");
            System.out.println("Authorization = " + token);
            token = token.replaceAll("Bearer ", "");

            // 토큰 검증
            if (!tokenProvider.validateToken(token)) {
                return TokenResponse.handleUnauthorizedRequest("유효하지 않은 토큰입니다.");
            }

            // token 을 통해 UserEntity 조회
            Optional<UserEntity> loggedInUserEntity = TokenResponse.getLoggedInUser(tokenProvider, token, userRepository);
            UserEntity userEntity = null;

            if (loggedInUserEntity.isPresent()) {
                userEntity = loggedInUserEntity.get();

                if (imageFiles == null) {
                    postService.save(anonymousPostDTO, null, userEntity);
                } else if (imageFiles.get(0).isEmpty()) {
                    responseJson.put("message", "선택된 이미지가 없습니다.");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseJson);
                } else {
                    // 이미지에 대한 요청이 제대로 온 경우
                    postService.save(anonymousPostDTO, imageFiles, userEntity);
                }

                responseJson.put("message", "익명커뮤니티에 게시물이 성공적으로 등록되었습니다.");

                return ResponseEntity.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseJson);

            } else {
                responseJson.put("message", "회원이 아닙니다.");

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseJson);

            }
        } catch (Exception e) {
            return ServerErrorResponse.handleServerError("서버에 예기치 않은 오류가 발생했습니다." + e);
        }
    }

    // 게시물 삭제 API
    @Transactional
    @DeleteMapping("/delete/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId, HttpServletRequest request) {

        responseJson = JsonNodeFactory.instance.objectNode();

        try {
            // 토큰 값 추출
            String token = request.getHeader("Authorization");
            token = token.replaceAll("Bearer ", "");

            // 토큰 검증
            if (!tokenProvider.validateToken(token)) {
                return TokenResponse.handleUnauthorizedRequest("유효하지 않은 토큰입니다.");
            } else {
                // Pid 이용하여 게시글 조회
                Post post = postRepository.findByPid(postId);

                if (post == null) {
                    return PostResponse.notExistPost("없거나 삭제된 게시글입니다.");
                }
                // 게시글 타입 체크
                if (post.getPostType() != PostType.A) {
                    responseJson.put("message", "익명 커뮤니티 게시글이 아닙니다.");

                    return ResponseEntity.status(HttpStatus.BAD_REQUEST) // 400
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseJson);
                } else {
                    AnonymousPost anonymousPost = (AnonymousPost) post;

                    // 헤더에 첨부되어 있는 token 에서 로그인 된 사용자 정보 받아옴
                    Authentication authentication = tokenProvider.getAuthentication(token);
                    UserDetails userDetails = (UserDetails) authentication.getPrincipal();

                    String loggedInUserId = userDetails.getUsername(); // UserDetails 객체에서 사용자 아이디를 가져옴
                    String postAuthorId = anonymousPost.getUser().getId();

                    // 본인이 작성한 게시글인지 확인
                    if (!loggedInUserId.equals(postAuthorId)) { // 본인이 작성한 게시글이 아닌 경우
                        responseJson.put("message", "본인이 작성한 게시글이 아닙니다.");

                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(responseJson);

                    } else { // 본인이 작성한 게시글인 경우
                        String message = postService.delete(anonymousPost); // 게시글 삭제
                        responseJson.put("message", message);
                        HttpStatus httpStatus = null;

                        if (message.equals("S3 에 저장되어 있지 않은 이미지가 있습니다.")) {
                            httpStatus = HttpStatus.NOT_FOUND;
                        } else if (message.equals("게시글을 성공적으로 삭제했습니다.")) {
                            httpStatus = HttpStatus.OK;
                        }

                        assert httpStatus != null;
                        return ResponseEntity.status(httpStatus)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(responseJson);
                    }
                }
            }
        } catch (Exception e) {
            return ServerErrorResponse.handleServerError("서버에 예기치 않은 오류가 발생했습니다." + e);
        }
    }

    // 게시물 수정 API
    @Transactional
    @PutMapping("/post/{postId}")
    public ResponseEntity<?> updatePost(@PathVariable Long postId, @RequestBody AnonymousPostDTO anonymousPostDTO, HttpServletRequest request) {

        responseJson = JsonNodeFactory.instance.objectNode();

        try {
            // 토큰 값 추출
            String token = request.getHeader("Authorization");
            token = token.replaceAll("Bearer ", "");

            // 토큰 검증
            if (!tokenProvider.validateToken(token)) {
                return TokenResponse.handleUnauthorizedRequest("유효하지 않은 토큰입니다.");
            } else {
                // Pid 이용하여 게시글 조회
                Post post = postRepository.findByPid(postId);

                if (post == null) {
                    responseJson.put("message", "없거나 삭제된 게시글입니다.");

                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseJson);
                }

                // 게시글 타입 체크
                if (post.getPostType() != PostType.A) {
                    responseJson.put("message", "익명 커뮤니티 게시글이 아닙니다.");

                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseJson);
                } else {
                    AnonymousPost anonymousPost = (AnonymousPost) post;

                    // 헤더에 첨부되어 있는 token 에서 로그인 된 사용자 정보 받아옴
                    Authentication authentication = tokenProvider.getAuthentication(token);
                    UserDetails userDetails = (UserDetails) authentication.getPrincipal();

                    String loggedInUserId = userDetails.getUsername(); // UserDetails 객체에서 사용자 아이디를 가져옴
                    String postAuthorId = anonymousPost.getUser().getId();

                    // 본인이 작성한 게시글인지 확인
                    if (!loggedInUserId.equals(postAuthorId)) { // 본인이 작성한 게시글이 아닌 경우
                        responseJson.put("message", "본인이 작성한 게시글이 아닙니다.");

                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(responseJson);

                    } else { // 본인이 작성한 게시글인 경우
                        postService.update(anonymousPostDTO, anonymousPost); // 게시글 수정

                        responseJson.put("message", "게시글을 성공적으로 수정했습니다.");

                        return ResponseEntity.status(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(responseJson);
                    }
                }
            }
        } catch (Exception e) {
            return ServerErrorResponse.handleServerError("서버에 예기치 않은 오류가 발생했습니다." + e);
        }
    }

    // 게시물 목록 조회 API
    @Transactional
    @GetMapping("/posts")
    public ResponseEntity<?> getPostList(HttpServletRequest request) {
        try {
            // 토큰 값 추출
            String token = request.getHeader("Authorization");
            token = token.replaceAll("Bearer ", "");

            // 토큰 검증
            if (!tokenProvider.validateToken(token)) {
                return TokenResponse.handleUnauthorizedRequest("유효하지 않은 토큰입니다.");
            } else {
                // Anonymous 타입만 가져오기
                List<AnonymousPost> anonymousPostList = (List<AnonymousPost>) postRepository.findByPostType(PostType.A);
                List<AnonymousPostDTO> anonymousPostDTOList = AnonymousPostDTO.toAnonymousPostDTOList(anonymousPostList);

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode anonymousPosts = objectMapper.convertValue(anonymousPostDTOList, JsonNode.class);

                responseJson = JsonNodeFactory.instance.objectNode();
                responseJson.set("posts", anonymousPosts);

                return ResponseEntity.status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseJson);
            }

        } catch (Exception e) {
            return ServerErrorResponse.handleServerError("서버에 예기치 않은 오류가 발생했습니다." + e);
        }
    }
}
