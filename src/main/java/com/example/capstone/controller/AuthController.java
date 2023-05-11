package com.example.capstone.controller;

import com.example.capstone.data.LoginResponse;
import com.example.capstone.data.ServerErrorResponse;
import com.example.capstone.dto.LoginDTO;
import com.example.capstone.dto.UserDTO;
import com.example.capstone.entity.UserEntity;
import com.example.capstone.jwt.JwtFilter;
import com.example.capstone.jwt.TokenProvider;
import com.example.capstone.repository.UserRepository;
import com.example.capstone.service.UserService;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@Api(tags = {"로그인 / 회원가입 API"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private ObjectNode responseJson;

    @ApiOperation(value = "로그인", notes = "아이디와 비밀번호를 입력받아 로그인합니다.", response = LoginResponse.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인에 성공했습니다.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserEntity.class))}),
            @ApiResponse(responseCode = "401", description = "비밀번호가 틀렸습니다.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserEntity.class))})
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO) {

        try {
            String userId = loginDTO.getId();
            String userPw = loginDTO.getPassword();

            // UserEntity를 사용자 아이디를 기반으로 조회
            Optional<UserEntity> loggedInUserEntity = userRepository.findById(userId); // 사용자 아이디를 기반으로 사용자 조회
            UserEntity userEntity = null;

            if (loggedInUserEntity.isPresent()) {
                userEntity = loggedInUserEntity.get();
                if (passwordEncoder.matches(loginDTO.getPassword(), userEntity.getPassword())) { // 비밀번호가 맞으면
                    // id와 userPassword로 authentication 토큰 생성
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userId, userPw);

                    //
                    Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    //jwt token 발행
                    String jwt = tokenProvider.createToken(authentication);

                    HttpHeaders httpHeaders = new HttpHeaders();
                    httpHeaders.add(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);

                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode user = objectMapper.convertValue(userEntity, JsonNode.class); // userEntity 객체 JsonNode 로 변환

                    responseJson = JsonNodeFactory.instance.objectNode();
                    responseJson.put("message", "로그인에 성공했습니다.");
                    responseJson.put("token", jwt);
                    responseJson.set("user", user);

                    return ResponseEntity.status(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseJson);

                } else { // 비밀번호 틀렸을 때
                    responseJson = JsonNodeFactory.instance.objectNode();
                    responseJson.put("message", "비밀번호가 틀렸습니다.");

                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseJson);
                }
            } else {
                responseJson = JsonNodeFactory.instance.objectNode();
                responseJson.put("message", "가입되어 있지 않은 사용자입니다.");

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseJson);
            }
        } catch (Exception e) {
            return ServerErrorResponse.handleServerError("서버에 예기치 않은 오류가 발생했습니다." + e);
        }
    }

    @ApiOperation(value = "회원가입", notes = "사용자 정보를 입력받아 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입에 성공했습니다.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserEntity.class))}),
            @ApiResponse(responseCode = "408", description = "이미 가입되어 있는 이메일입니다.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserEntity.class))}),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 아이디입니다.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserEntity.class))}),
            @ApiResponse(responseCode = "500", description = "서버에 문제가 생겼습니다.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserEntity.class))})
    })
    @PostMapping("/join")
    public ResponseEntity<?> join(@RequestBody UserDTO userDTO) {

       /* {
            "id": "1111",
            "password": "1111",
            "name": "홍길동",
            "nickName": "홍길동",
            "birth": "1999-10-13",
            "email": "aaaa@naver.com",
            "phoneNum": "01011112222",
            "address": "서울시 성북구",
            "gender": "여자"
        }*/

        System.out.println("AuthController.join");

        try {
            if (userService.isUserIdExists(userDTO.getId()) != null) { // 이미 존재하는 아이디
                responseJson = JsonNodeFactory.instance.objectNode();
                responseJson.put("message", "이미 존재하는 아이디입니다.");

                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseJson);

            } else if (userService.isUserEmailExists(userDTO.getEmail()) != null) {
                responseJson = JsonNodeFactory.instance.objectNode();
                responseJson.put("message", "이미 가입되어 있는 이메일입니다.");

                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseJson);

            } else {
                userService.join(userDTO);
                responseJson = JsonNodeFactory.instance.objectNode();
                responseJson.put("message", "회원가입에 성공했습니다.");

                return ResponseEntity.status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseJson);
            }
        } catch (Exception e) {
            return ServerErrorResponse.handleServerError("서버에 예기치 않은 오류가 발생했습니다." + e);
        }
    }
}
