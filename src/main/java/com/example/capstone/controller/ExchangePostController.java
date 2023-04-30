package com.example.capstone.controller;

import com.example.capstone.data.LoginResponse;
import com.example.capstone.dto.ExchangePostDTO;
import com.example.capstone.entity.ExchangePost;
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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Api(tags = {"재능교환 게시물 관련 API"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/exchange")
public class ExchangePostController {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final TokenProvider tokenProvider;
    private final PostService postService;
    private ObjectNode responseJson;

    // 게시물 등록 API
    @ApiOperation(value = "재능교환 게시물 등록", notes = "재능교환 게시물을 등록합니다.", response = LoginResponse.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "재능거래 게시물이 성공적으로 등록되었습니다.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExchangePost.class))}),
            @ApiResponse(responseCode = "400", description = "재능거래 게시물 등록에 실패했습니다.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExchangePost.class))})
    })

    @PostMapping("/post")
    public ResponseEntity<?> createPost(@Valid @RequestBody ExchangePostDTO exchangePostDTO, BindingResult bindingResult, HttpServletRequest request) {

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
            System.out.println("token 값 = " + token);

            // 토큰 검증
            if (!tokenProvider.validateToken(token)) {
                responseJson.put("message", "유효하지 않은 토큰입니다.");

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseJson);
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
                System.out.println("User = " + userEntity);
                System.out.println("uid = " + uid);

                // 가져온 Uid 를 해당 포스트 컬럼에 추가
                postService.save(exchangePostDTO, userEntity);

                responseJson.put("message", "재능거래 게시물이 성공적으로 등록되었습니다.");

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
            responseJson.put("message", "서버에 예기치 않은 오류가 발생했습니다." + e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(responseJson);
        }
    }

    @Transactional
    @DeleteMapping("/delete/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId, HttpServletRequest request) {

        System.out.println("delete_postId = " + postId);

        responseJson = JsonNodeFactory.instance.objectNode();

        try {
            // 토큰 값 추출
            String token = request.getHeader("Authorization");
            token = token.replaceAll("Bearer ", "");
//            System.out.println("token = " + token);

            // 토큰 검증
            if (!tokenProvider.validateToken(token)) {
                responseJson.put("message", "유효하지 않은 토큰입니다.");

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseJson);
            } else {
                // Pid 이용하여 게시글 조회
                ExchangePost exchangePost = (ExchangePost) postRepository.findByPid(postId);

                if (exchangePost == null) {
                    responseJson.put("message", "없거나 삭제된 게시글입니다.");

                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseJson);

                } else {
                    // 헤더에 첨부되어 있는 token 에서 로그인 된 사용자 정보 받아옴
                    Authentication authentication = tokenProvider.getAuthentication(token);
                    UserDetails userDetails = (UserDetails) authentication.getPrincipal();

                    String loggedInUserId = userDetails.getUsername(); // UserDetails 객체에서 사용자 아이디를 가져옴
                    String postAuthorId = exchangePost.getUser().getId();

                    // 본인이 작성한 게시글인지 확인
                    if (!loggedInUserId.equals(postAuthorId)) { // 본인이 작성한 게시글이 아닌 경우
                        responseJson.put("message", "본인이 작성한 게시글이 아닙니다.");

                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(responseJson);

                    } else { // 본인이 작성한 게시글인 경우
                        postService.delete(exchangePost); // 게시글 삭제

                        responseJson.put("message", "게시글을 성공적으로 삭제했습니다.");

                        return ResponseEntity.status(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(responseJson);

                    }
                }
            }
        } catch (Exception e) {
            responseJson.put("message", "서버에 예기치 않은 오류가 발생했습니다." + e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(responseJson);
        }
    }

    @Transactional
    @PutMapping("/post/{postId}")
    public ResponseEntity<?> updatePost(@PathVariable Long postId, @RequestBody ExchangePostDTO exchangePostDTO, HttpServletRequest request) {

        responseJson = JsonNodeFactory.instance.objectNode();

        try {
            // 토큰 값 추출
            String token = request.getHeader("Authorization");
            token = token.replaceAll("Bearer ", "");

            // 토큰 검증
            if (!tokenProvider.validateToken(token)) {
                responseJson.put("message", "유효하지 않은 토큰입니다.");

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseJson);
            } else {
                // Pid 이용하여 게시글 조회
                ExchangePost exchangePost = (ExchangePost) postRepository.findByPid(postId);

                if (exchangePost == null) {
                    responseJson.put("message", "없거나 삭제된 게시글입니다.");

                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseJson);
                } else {
                    // 헤더에 첨부되어 있는 token 에서 로그인 된 사용자 정보 받아옴
                    Authentication authentication = tokenProvider.getAuthentication(token);
                    UserDetails userDetails = (UserDetails) authentication.getPrincipal();

                    String loggedInUserId = userDetails.getUsername(); // UserDetails 객체에서 사용자 아이디를 가져옴
                    String postAuthorId = exchangePost.getUser().getId();

                    // 본인이 작성한 게시글인지 확인
                    if (!loggedInUserId.equals(postAuthorId)) { // 본인이 작성한 게시글이 아닌 경우
                        responseJson.put("message", "본인이 작성한 게시글이 아닙니다.");

                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(responseJson);

                    } else { // 본인이 작성한 게시글인 경우
                        postService.update(exchangePostDTO, exchangePost); // 게시글 수정

                        responseJson.put("message", "게시글을 성공적으로 수정했습니다.");

                        return ResponseEntity.status(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(responseJson);
                    }
                }
            }
        } catch (Exception e) {
            responseJson.put("message", "서버에 예기치 않은 오류가 발생했습니다." + e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(responseJson);
        }
    }

    @Transactional
    @GetMapping("/posts")
    public ResponseEntity<?> getPostList(HttpServletRequest request) {
        try {
            // 토큰 값 추출
            String token = request.getHeader("Authorization");
            token = token.replaceAll("Bearer ", "");

            // 토큰 검증
            if (!tokenProvider.validateToken(token)) {
                responseJson.put("message", "유효하지 않은 토큰입니다.");

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseJson);
            } else {
                // exchangePost 타입만 가져오기
                List<ExchangePost> exchangePostList = (List<ExchangePost>) postRepository.findByPostType(PostType.T);
                List<ExchangePostDTO> exchangePostDTOList = ExchangePostDTO.toExchangePostDTOList(exchangePostList);

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode exchangePosts = objectMapper.convertValue(exchangePostDTOList, JsonNode.class);

                responseJson = JsonNodeFactory.instance.objectNode();
                responseJson.set("posts", exchangePosts);

                return ResponseEntity.status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseJson);
            }

        } catch (Exception e) {
            responseJson = JsonNodeFactory.instance.objectNode();
            responseJson.put("message", "서버에 예기치 않은 오류가 발생했습니다." + e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(responseJson);
        }
    }

    @Transactional
    @GetMapping("/detail/{postId}")
    public ResponseEntity<?> getPostDetail(@PathVariable Long postId, HttpServletRequest request) {
        try {
            // 토큰 값 추출
            String token = request.getHeader("Authorization");
            token = token.replaceAll("Bearer ", "");

            // 토큰 검증
            if (!tokenProvider.validateToken(token)) {
                responseJson = JsonNodeFactory.instance.objectNode();
                responseJson.put("message", "유효하지 않은 토큰입니다.");

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseJson);
            } else {
                ExchangePost exchangePost = (ExchangePost) postRepository.findByPid(postId);

                if (exchangePost == null) {
                    responseJson = JsonNodeFactory.instance.objectNode();
                    responseJson.put("message", "없거나 삭제된 게시글입니다.");

                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseJson);
                } else {
                    ExchangePostDTO exchangePostDTO = ExchangePostDTO.toExchangePostDTO(exchangePost);
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode exchangePostDetail = objectMapper.convertValue(exchangePostDTO, JsonNode.class);
                    System.out.println("exchangePostDetail = " + exchangePostDetail);

                    return ResponseEntity.status(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(exchangePostDetail);
                }
            }

        } catch (Exception e) {
            responseJson = JsonNodeFactory.instance.objectNode();
            responseJson.put("message", "서버에 예기치 않은 오류가 발생했습니다." + e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(responseJson);
        }
    }
}