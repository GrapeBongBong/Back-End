package com.example.capstone.controller;

import com.example.capstone.data.BasicResponse;
import com.example.capstone.dto.UserProfileDTO;
import com.example.capstone.jwt.TokenProvider;
import com.example.capstone.service.UserProfileService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Api(tags = {"프로필 수정 관련 API"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserProfileController {
    private final UserProfileService userProfileService;
    private final TokenProvider tokenProvider;
    private BasicResponse basicResponse = new BasicResponse();

    @PutMapping("/{userId}/profile")
    public ResponseEntity<?> updateUserProfile(@PathVariable String userId, @RequestBody UserProfileDTO userProfileDTO, HttpServletRequest request) {
        try {
            // 토큰 값 추출
            String token = request.getHeader("Authorization");
            token = token.replaceAll("Bearer ", "");

            // 토큰 검증
            if (!tokenProvider.validateToken(token)) {
                basicResponse = BasicResponse.builder()
                        .code(401)
                        .httpStatus(HttpStatus.UNAUTHORIZED)
                        .message("유효하지 않은 토큰입니다.")
                        .build();
            } else {
                // 헤더에 첨부되어 있는 token 에서 로그인 된 사용자 정보 받아옴
                Authentication authentication = tokenProvider.getAuthentication(token);
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();

                String loggedInUserId = userDetails.getUsername(); // UserDetails 객체에서 사용자 아이디를 가져옴
                System.out.println(loggedInUserId);
                // 유저 프로필 수정
                UserProfileDTO updatedUserProfile = userProfileService.updateUserProfile(loggedInUserId, userProfileDTO);

                basicResponse = BasicResponse.builder()
                        .code(200)
                        .httpStatus(HttpStatus.OK)
                        .message("유저 프로필을 성공적으로 수정했습니다.")
                        .build();
            }
        } catch (Exception e) {
            basicResponse = BasicResponse.builder()
                    .code(500)
                    .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("서버에 에러가 발생했습니다." + e)
                    .build();
        }

        return new ResponseEntity<>(basicResponse, basicResponse.getHttpStatus());
    }
}
