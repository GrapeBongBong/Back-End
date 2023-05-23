package com.example.capstone.controller;

import com.example.capstone.data.ServerErrorResponse;
import com.example.capstone.data.TokenResponse;
import com.example.capstone.dto.UserProfileDTO;
import com.example.capstone.entity.UserEntity;
import com.example.capstone.jwt.TokenProvider;
import com.example.capstone.repository.UserRepository;
import com.example.capstone.service.UserProfileService;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;

@Api(tags = {"프로필 수정 관련 API"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserProfileController {
    private final UserRepository userRepository;
    private final UserProfileService userProfileService;
    private final TokenProvider tokenProvider;
    private ObjectNode responseJson;

    @PutMapping("/profile/{userId}")
    public ResponseEntity<?> updateUserProfile(@PathVariable String userId, @RequestBody UserProfileDTO userProfileDTO, HttpServletRequest request) {

        try {
            // 토큰 값 추출
            String token = request.getHeader("Authorization");
            token = token.replaceAll("Bearer ", "");

            // 토큰 검증
            if (!tokenProvider.validateToken(token)) {
                return TokenResponse.handleUnauthorizedRequest("유효하지 않은 토큰입니다.");

            } else {
                // 헤더에 첨부되어 있는 token 에서 로그인 된 사용자 정보 받아옴
                Authentication authentication = tokenProvider.getAuthentication(token);
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();

                String loggedInUserId = userDetails.getUsername(); // UserDetails 객체에서 사용자 아이디를 가져옴
                System.out.println(loggedInUserId);
                // 유저 프로필 수정
                UserProfileDTO updatedUserProfile = userProfileService.updateUserProfile(loggedInUserId, userProfileDTO);

                responseJson = JsonNodeFactory.instance.objectNode();
                responseJson.put("message", "사용자 프로필을 성공적으로 수정했습니다.");

                return ResponseEntity.status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseJson);
            }
        } catch (Exception e) {
            return ServerErrorResponse.handleServerError("서버에 예기치 않은 오류가 발생했습니다." + e);
        }
    }

    // 사용자 프로필 업로드 API
    @PutMapping("/profile/image")
    public ResponseEntity<?> profileImageUpload(@RequestParam("image") MultipartFile multipartFile, HttpServletRequest request) {
        try {
            responseJson = JsonNodeFactory.instance.objectNode();

            // 토큰 값 추출
            String token = request.getHeader("Authorization");
            token = token.replaceAll("Bearer ", "");

            // 토큰 검증
            if (!tokenProvider.validateToken(token)) {
                return TokenResponse.handleUnauthorizedRequest("유효하지 않은 토큰입니다.");
            } else {
                if (multipartFile.isEmpty()) {
                    responseJson.put("message", "선택된 이미지가 없습니다.");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseJson);
                } else { // 이미지 제대로 도착
                    // 토큰에서 로그인된 사용자 정보 가져오기
                    Authentication authentication = tokenProvider.getAuthentication(token);
                    UserDetails userDetails = (UserDetails) authentication.getPrincipal();

                    // 사용자 아이디 가져오기
                    String userId = userDetails.getUsername();
                    Optional<UserEntity> user = userRepository.findById(userId);
                    if (user.isPresent()) {
                        String imageUrl = userProfileService.uploadProfileImage(multipartFile, user.get());
                        responseJson.put("profileImage", imageUrl);
                        return ResponseEntity.status(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(responseJson);
                    } else {
                        responseJson.put("message", "가입된 사용자가 아닙니다.");

                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(responseJson);
                    }
                }
            }
        } catch (IOException e) {
            return ServerErrorResponse.handleServerError("서버에 예기치 않은 오류가 발생했습니다." + e);
        }
    }
}
