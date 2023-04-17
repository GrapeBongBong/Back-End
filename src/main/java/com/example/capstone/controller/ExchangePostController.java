package com.example.capstone.controller;

import com.example.capstone.data.DataResponse;
import com.example.capstone.dto.ExchangePostDTO;
import com.example.capstone.entity.ExchangePost;
import com.example.capstone.entity.UserEntity;
import com.example.capstone.jwt.TokenProvider;
import com.example.capstone.repository.PostRepository;
import com.example.capstone.repository.UserRepository;
import com.example.capstone.service.PostService;
import io.jsonwebtoken.Claims;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Api(tags = {"재능교환 게시물 관련 API"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/exchange")
public class ExchangePostController {

    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final PostService postService;

    // 게시물 등록 API
    @ApiOperation(value = "재능교환 게시물 등록", notes = "재능교환 게시물을 등록합니다.", response = DataResponse.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "재능거래 게시물이 성공적으로 등록되었습니다.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExchangePost.class))}),
            @ApiResponse(responseCode = "400", description = "재능거래 게시물 등록에 실패했습니다.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExchangePost.class))})
    })
    @PostMapping("/post")
    public ResponseEntity<DataResponse> createPost(@Valid @RequestBody ExchangePostDTO exchangePostDTO, HttpServletRequest request) {

        System.out.println("ExchangePostController.post");

        DataResponse dataResponse = new DataResponse();

        try {
            // 토큰 값 추출
            String token = request.getHeader("Authorization");
            System.out.println("Authorization = " + token);

            // 헤더에 첨부되어 있는 token 에서 로그인 된 사용자 정보 받아옴
            Authentication authentication = tokenProvider.getAuthentication(token);
            UserDetails user = (UserDetails) authentication.getPrincipal();
            String id = authentication.getName();
            System.out.println("User = " + user);
            System.out.println("id = " + id);

            // UserEntity 프록시 객체를 가져온다.
            UserEntity userEntity = userRepository.getOne(Long.valueOf(id));

            postService.save(exchangePostDTO, userEntity);

           /* Claims claims= (Claims)tokenProvider.getAuthentication(token);
            System.out.println("clams = " + claims);
            String userId = claims.get("userId", String.class);
            System.out.println("가져온 userId = " + userId);*/

            /*// ExchangePostDTO 에서 Uid 값을 이용해 UserEntity 객체 조회
            UserEntity user = userRepository.findById(exchangePostDTO.getUid()).orElseThrow();*/

            dataResponse = DataResponse.builder()
                    .code(200)
                    .httpStatus(HttpStatus.OK)
                    .message("재능거래 게시물이 성공적으로 등록되었습니다.")
                    .build();

        } catch (Exception e) {
            dataResponse = DataResponse.builder()
                    .code(500)
                    .httpStatus(HttpStatus.UNAUTHORIZED)
                    .message("서버에 에러가 발생했습니다." + e)
                    .build();
        }

        return new ResponseEntity<>(dataResponse, dataResponse.getHttpStatus());
    }
}
