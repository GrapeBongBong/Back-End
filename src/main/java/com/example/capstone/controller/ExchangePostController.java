package com.example.capstone.controller;

import com.example.capstone.data.LoginResponse;
import com.example.capstone.data.PostResponse;
import com.example.capstone.data.ServerErrorResponse;
import com.example.capstone.data.TokenResponse;
import com.example.capstone.dto.AnonymousPostDTO;
import com.example.capstone.dto.ExchangePostDTO;
import com.example.capstone.dto.PageResponse;
import com.example.capstone.dto.SearchDTO;
import com.example.capstone.entity.*;
import com.example.capstone.jwt.TokenProvider;
import com.example.capstone.repository.ExchangePostRepository;
import com.example.capstone.repository.PostRepository;
import com.example.capstone.repository.UserRepository;
import com.example.capstone.service.LikePostService;
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
import io.swagger.v3.oas.annotations.servers.Server;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Api(tags = {"재능교환 게시물 관련 API"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/exchange")
@Slf4j
public class ExchangePostController {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final TokenProvider tokenProvider;
    private final PostService postService;
    private final LikePostService likePostService;
    private ObjectNode responseJson;

    // 게시물 등록 API
    @ApiOperation(value = "재능교환 게시물 등록", notes = "재능교환 게시물을 등록합니다.", response = LoginResponse.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "재능거래 게시물이 성공적으로 등록되었습니다.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExchangePost.class))}),
            @ApiResponse(responseCode = "400", description = "재능거래 게시물 등록에 실패했습니다.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExchangePost.class))})
    })

    @PostMapping(value = "/post", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> createPost(@Valid @RequestPart ExchangePostDTO exchangePostDTO,
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
                    postService.save(exchangePostDTO, null, userEntity);
                } else if (imageFiles.get(0).isEmpty()) {
                    responseJson.put("message", "선택된 이미지가 없습니다.");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseJson);
                } else {
                    // 이미지에 대한 요청이 제대로 온 경우
                    postService.save(exchangePostDTO, imageFiles, userEntity);
                }

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
                if (post.getPostType() != PostType.T) {
                    responseJson.put("message", "재능거래 게시글이 아닙니다.");

                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseJson);
                } else {
                    ExchangePost exchangePost = (ExchangePost) post;

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
                        String message = postService.delete(exchangePost); // 게시글 삭제
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
    public ResponseEntity<?> updatePost(@PathVariable Long postId, @RequestBody ExchangePostDTO exchangePostDTO, HttpServletRequest request) {

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
                if (post.getPostType() != PostType.T) {
                    responseJson.put("message", "재능거래 게시글이 아닙니다.");

                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseJson);
                } else {
                    ExchangePost exchangePost = (ExchangePost) post;

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
            return ServerErrorResponse.handleServerError("서버에 예기치 않은 오류가 발생했습니다." + e);
        }
    }

    // 게시물 목록 조회 API
    @Transactional
    @GetMapping("/posts")
    public ResponseEntity<?> getPostList(HttpServletRequest request) {
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
                    responseJson.put("message", "가입된 사용자가 아닙니다.");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseJson);
                }
                // exchangePost 타입만 가져오기
                List<Post> exchangePostList = postRepository.findByPostType(PostType.T);

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

    //재능 교환 게시물 페이징 처리
    @Transactional
    @GetMapping("/exchange-posts")
    public ResponseEntity<?> getPostList(Pageable pageable, HttpServletRequest request) {
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
                    responseJson.put("message", "가입된 사용자가 아닙니다.");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseJson);
                }

                // exchangePost 타입만 가져오기
                Page<ExchangePost> exchangePostPage = postRepository.findByPostType(PostType.T, pageable);
                List<ExchangePost> postList = exchangePostPage.getContent();

                List<ExchangePost> exchangePostList = new ArrayList<>();
                for (Post post : postList) {
                    if (post instanceof ExchangePost) {
                        exchangePostList.add((ExchangePost) post);
                    }
                }

                // 현재 로그인된 사용자가 해당 게시글에 좋아요를 눌렀는지 체크
                List<Boolean> isLikedList = likePostService. getIsLikedForExchangePostList(user.get(), exchangePostList);

                // 변환된 ExchangePostDTO 리스트와 페이지 정보를 담은 객체 생성
                PageResponse<ExchangePostDTO> pageResponse = PageResponse.from(exchangePostPage.map(exchangePost -> {
                    // 해당 게시글이 exchangePostList에 존재하는지 확인하고 인덱스 가져오기
                    int index = exchangePostList.indexOf(exchangePost);
                    // isLikedList에서 해당 인덱스의 값을 가져오고, 존재하지 않는 경우 false
                    boolean isLiked = (index != -1) ? isLikedList.get(index) : false;
                    // ExchangePostDTO로 변환하여 반환
                    return ExchangePostDTO.toExchangePostDTO(exchangePost, isLiked);
                }));

                return ResponseEntity.status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(pageResponse);
            }

        } catch (Exception e) {
            return ServerErrorResponse.handleServerError("서버에 예기치 않은 오류가 발생했습니다." + e);
        }
    }

    // 게시글 상세 조회 API
    /*@Transactional
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
    }*/

    // '좋아요' 추가 API
    @PostMapping("/{postId}/like")
    public ResponseEntity<?> likePost(@PathVariable Long postId, HttpServletRequest request) {
        try {
            responseJson = JsonNodeFactory.instance.objectNode();
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
                Optional<UserEntity> user = TokenResponse.getLoggedInUser(tokenProvider, token, userRepository);

                if (user.isEmpty()) {
                    responseJson.put("message", "가입된 사용자가 아닙니다.");

                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseJson);
                }
                // postId 에 해당하는 게시물 조회
                Post post = postRepository.findByPid(postId);
                if (post == null) {
                    responseJson.put("message", "없거나 삭제된 게시글입니다.");

                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseJson);
                }

                if (post.getPostType() != PostType.T) {
                    responseJson.put("message", "재능 거래 게시글이 아닙니다.");

                    return ResponseEntity.status(HttpStatus.BAD_REQUEST) // 400
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseJson);
                }

                // 이미 좋아요를 한 게시글인지 체크
                boolean isLiked = likePostService.isLiked(user.get(), post);
                if (isLiked) {
                    responseJson.put("message", "이미 좋아요를 한 게시글입니다.");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseJson);
                }

                likePostService.likePostByUser(user.get(), post);

                responseJson.put("message", "게시물에 좋아요를 눌렀습니다.");
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

    // '좋아요' 취소 API
    @Transactional
    @PostMapping("/{postId}/unlike")
    public ResponseEntity<?> unlikePost(@PathVariable Long postId, HttpServletRequest request) {
        try {
            responseJson = JsonNodeFactory.instance.objectNode();
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
                Optional<UserEntity> user = TokenResponse.getLoggedInUser(tokenProvider, token, userRepository);

                if (user.isEmpty()) {
                    responseJson.put("message", "가입된 사용자가 아닙니다.");

                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseJson);
                }
                // postId 에 해당하는 게시물 조회
                Post post = postRepository.findByPid(postId);
                if (post == null) {
                    responseJson.put("message", "없거나 삭제된 게시글입니다.");

                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseJson);
                }

                if (post.getPostType() != PostType.T) {
                    responseJson.put("message", "재능 거래 게시글이 아닙니다.");

                    return ResponseEntity.status(HttpStatus.BAD_REQUEST) // 400
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseJson);
                }

                // 좋아요를 한 게시글인지 체크 (좋아요를 한 게시글에 대해서 취소)
                boolean isLiked = likePostService.isLiked(user.get(), post);
                if (isLiked) {
                    likePostService.unlikePostByUser(user.get(), post);
                    responseJson.put("message", "해당 게시글에 좋아요를 취소했습니다.");
                    return ResponseEntity.status(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseJson);
                } else {
                    responseJson.put("message", "좋아요를 한 게시글이 아닙니다.");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseJson);
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

    // 게시물 카테고리별 검색 API
    @Transactional
    @PostMapping("/search")
    public ResponseEntity<?> searchPost(@RequestBody SearchDTO searchDTO, HttpServletRequest request) {
        try {
            responseJson = JsonNodeFactory.instance.objectNode();
            // 토큰 값 추출
            String token = request.getHeader("Authorization");
            token = token.replaceAll("Bearer ", "");

            // 토큰 검증
            if (!tokenProvider.validateToken(token)) {
                return TokenResponse.handleUnauthorizedRequest("유효하지 않은 토큰입니다.");
            } else {
                Optional<UserEntity> user = TokenResponse.getLoggedInUser(tokenProvider, token, userRepository);

                if (user.isEmpty()) {
                    responseJson.put("message", "가입된 사용자가 아닙니다.");

                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseJson);
                }

                String giveCate = searchDTO.getGiveCate();
                String takeCate = searchDTO.getTakeCate();

                List<ExchangePost> exchangePostList = postService.searchPostByCategory(giveCate, takeCate);
                List<Post> postList = new ArrayList<>(exchangePostList);

                // 현재 로그인된 사용자가 해당 게시글에 좋아요를 눌렀는지 체크
                List<Boolean> isLikedList = likePostService.getIsLiked(user.get(), postList);

                List<ExchangePostDTO> exchangePostDTOList = ExchangePostDTO.toExchangePostDTOList(postList, isLikedList);

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

    // '좋아요' 많은 순으로 5개씩 게시물 목록 반환 API
    @GetMapping("/popular")
    @Transactional
    public ResponseEntity<?> getPopularPosts(@RequestParam(name = "page", defaultValue = "1") int page, HttpServletRequest request) {
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
            }
            // 사용자 정보 가져오기
            Optional<UserEntity> user = TokenResponse.getLoggedInUser(tokenProvider, token, userRepository);
            if (user.isEmpty()) {
                responseJson.put("message", "가입된 사용자가 아닙니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseJson);
            }
            // 재능 교환 게시물 타입만 가져오기
            List<Post> exchangePostList = postRepository.findByPostType(PostType.T);
            // 현재 로그인된 사용자가 해당 게시글에 좋아요를 눌렀는지 체크
            List<Boolean> isLikedList = likePostService.getIsLiked(user.get(), exchangePostList);
            // 좋아요 순으로 정렬된 게시물 목록 가져옴
            List<Post> popularPosts = postService.getPopularExchangePosts(page);
            // 필요한 DTO 객체로 변환
            List<ExchangePostDTO> postDTOList = ExchangePostDTO.toExchangePostDTOList(popularPosts, isLikedList);

            // DTO 객체를 JSON 형식으로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode exchangePostsPages = objectMapper.convertValue(postDTOList, JsonNode.class);

            // 게시물 타입 확인
            for (Post post : popularPosts) {
                if (post.getPostType() != PostType.T) {
                    responseJson.put("message", "재능 교환 게시물이 아닙니다.");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseJson);
                }
            }

            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(exchangePostsPages);

        } catch (Exception e) {
            // 예외 발생 시 에러 응답 반환
            responseJson.put("message", "서버에 예기치 않은 오류가 발생했습니다."+e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(responseJson);
        }
    }
}