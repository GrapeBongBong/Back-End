package com.example.capstone.controller;

import com.example.capstone.data.ServerErrorResponse;
import com.example.capstone.data.TokenResponse;
import com.example.capstone.entity.ExchangePost;
import com.example.capstone.entity.Match;
import com.example.capstone.entity.Post;
import com.example.capstone.jwt.TokenProvider;
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
    private ObjectNode responseJson;

    // 매칭 진행 API
    @ApiOperation(value = "매칭 진행", notes = "재능 교환 게시물의 매칭을 진행합니다.", response = ResponseEntity.class)
    @PostMapping("/match/{postId}")
    public ResponseEntity<?> processMatch(@PathVariable Long postId, HttpServletRequest request) {
        responseJson = JsonNodeFactory.instance.objectNode();

        try {
            // 토큰 값 추출
            String token = request.getHeader("Authorization");
            token = token.replaceAll("Bearer ", "");

            // 토큰 검증
            if (!tokenProvider.validateToken(token)) {
                return TokenResponse.handleUnauthorizedRequest("유효하지 않은 토큰입니다.");
            }

            // 매칭 진행 로직
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

                // 매칭 진행
                Match match = new Match();
                match.setExchangePost(exchangePost);

                matchRepository.save(match);

                // 재능 교환 게시물의 상태를 0으로 변경
                exchangePost.setStatus(false);
               postRepository.save(exchangePost);

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
        } catch (Exception e) {
            return ServerErrorResponse.handleServerError("서버에 예기치 않은 오류가 발생했습니다." + e);
        }
    }


}
