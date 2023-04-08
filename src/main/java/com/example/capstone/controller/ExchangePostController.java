package com.example.capstone.controller;

import com.example.capstone.data.DataResponse;
import com.example.capstone.dto.ExchangePostDTO;
import com.example.capstone.entity.ExchangePost;
import com.example.capstone.entity.UserEntity;
import com.example.capstone.repository.PostRepository;
import com.example.capstone.repository.UserRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Api(tags = {"재능교환 게시물 관련 API"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/exchange")
public class ExchangePostController {

    private final UserRepository userRepository;
    private final PostRepository postRepository;

    // 게시물 등록 API
    @ApiOperation(value = "재능교환 게시물 등록", notes = "재능교환 게시물을 등록합니다.", response = DataResponse.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "재능거래 게시물이 성공적으로 등록되었습니다.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExchangePost.class))}),
            @ApiResponse(responseCode = "400", description = "재능거래 게시물 등록에 실패했습니다.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ExchangePost.class))})
    })
    @PostMapping("/post")
    public ResponseEntity<DataResponse> doPost(@Valid @RequestBody ExchangePostDTO exchangePostDTO) {

        System.out.println("ExchangePostController.post");

        DataResponse dataResponse = new DataResponse();

        try {
            // ExchangePostDTO 에서 Uid 값을 이용해 UserEntity 객체 조회
            UserEntity user = userRepository.findById(exchangePostDTO.getUid()).orElseThrow();

            ExchangePost exchangePost = ExchangePost.toExchangePost(exchangePostDTO, user);
            postRepository.save(exchangePost);

            dataResponse = DataResponse.builder()
                    .code(200)
                    .httpStatus(HttpStatus.OK)
                    .message("재능거래 게시물이 성공적으로 등록되었습니다.")
                    .build();

        } catch (Exception e) {

        }

        return new ResponseEntity<>(dataResponse, dataResponse.getHttpStatus());
    }
}
