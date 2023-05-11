package com.example.capstone.data;

import com.example.capstone.entity.UserEntity;
import com.example.capstone.jwt.TokenProvider;
import com.example.capstone.repository.UserRepository;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;


public class TokenResponse {
    private static final ObjectNode responseJson = JsonNodeFactory.instance.objectNode();

    public static ResponseEntity<?> handleUnauthorizedRequest(String message) {

        responseJson.put("message", message);

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseJson);
    }

    public static Optional<UserEntity> getLoggedInUser(TokenProvider tokenProvider, String token, UserRepository userRepository) {
        // 헤더에 첨부되어 있는 token 에서 로그인 된 사용자 정보 받아옴
        Authentication authentication = tokenProvider.getAuthentication(token);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String id = userDetails.getUsername(); // UserDetails 객체에서 사용자 아이디를 가져옴

        // UserEntity 를 사용자 아이디를 기반으로 조회
        return userRepository.findById(id);
    }
}
