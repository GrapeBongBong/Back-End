package com.example.capstone.controller;

import com.example.capstone.dto.AnonymousPostDTO;
import com.example.capstone.dto.ExchangePostDTO;
import com.example.capstone.entity.AnonymousPost;
import com.example.capstone.entity.ExchangePost;
import com.example.capstone.entity.Post;
import com.example.capstone.entity.PostType;
import com.example.capstone.jwt.TokenProvider;
import com.example.capstone.repository.PostRepository;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(tags = {"프로필 내 본인 게시물 조회 API"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/profile")
@Slf4j
public class PostViewController {

    private final PostRepository postRepository;
    private final TokenProvider tokenProvider;

    // 사용자가 작성한 재능 교환 게시물 조회 API
    @GetMapping("/exchange")
    @Transactional
    public ResponseEntity<?> getExchangePosts(HttpServletRequest request) {
        ObjectNode responseJson = JsonNodeFactory.instance.objectNode();

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
                // 토큰에서 로그인된 사용자 정보 가져오기
                Authentication authentication = tokenProvider.getAuthentication(token);
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();

                // 사용자 이름 가져오기
                String username = userDetails.getUsername();

                // 사용자 이름, 교환 게시물 가져오기
                List<ExchangePost> exchangePostList = (List<ExchangePost>) postRepository.findByUserIdAndPostType(username, PostType.T);
                List<ExchangePostDTO> exchangePostDTOList = ExchangePostDTO.toExchangePostDTOList(exchangePostList);

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode exchangePosts = objectMapper.convertValue(exchangePostDTOList, JsonNode.class);

                return ResponseEntity.status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(exchangePosts);
            }
        } catch (Exception e) {
            responseJson.put("message", "서버에 예기치 않은 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(responseJson);
        }
    }

    // 사용자가 작성한 익명 게시물 조회 API
    @GetMapping("/anonymous")
    @Transactional
    public ResponseEntity<?> getAnonymousPosts(HttpServletRequest request) {
        ObjectNode responseJson = JsonNodeFactory.instance.objectNode();

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
                // 토큰에서 로그인된 사용자 정보 가져오기
                Authentication authentication = tokenProvider.getAuthentication(token);
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();

                // 사용자 이름 가져오기
                String username = userDetails.getUsername();

                // 사용자 이름, 교환 게시물 가져오기
                List<AnonymousPost> anonymousPostList = (List<AnonymousPost>) postRepository.findByUserIdAndPostType(username, PostType.A);
                List<AnonymousPostDTO> anonymousPostDTOList = AnonymousPostDTO.toAnonymousPostDTOList(anonymousPostList);

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode anonymousPosts = objectMapper.convertValue(anonymousPostDTOList, JsonNode.class);

                return ResponseEntity.status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(anonymousPosts);
            }
        } catch (Exception e) {
            responseJson.put("message", "서버에 예기치 않은 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(responseJson);
        }
    }

}
