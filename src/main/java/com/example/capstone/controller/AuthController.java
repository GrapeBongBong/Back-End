package com.example.capstone.controller;

import com.example.capstone.data.DataResponse;
import com.example.capstone.dto.LoginDTO;
import com.example.capstone.dto.TokenDTO;
import com.example.capstone.dto.UserDTO;
import com.example.capstone.data.BasicResponse;
import com.example.capstone.entity.RoleEntity;
import com.example.capstone.entity.UserEntity;
import com.example.capstone.jwt.JwtFilter;
import com.example.capstone.jwt.TokenProvider;
import com.example.capstone.repository.UserRepository;
import com.example.capstone.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = {"로그인 / 회원가입 API"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final PasswordEncoder bCryptPasswordEncoder;

    @ApiOperation(value = "로그인", notes = "아이디와 비밀번호를 입력받아 로그인합니다.", response = DataResponse.class)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인에 성공했습니다.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserEntity.class))}),
            @ApiResponse(responseCode = "401", description = "비밀번호가 틀렸습니다.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserEntity.class))})
    })
    @PostMapping("/login")
    public ResponseEntity<DataResponse> login(@Valid @RequestBody LoginDTO loginDTO) {

        DataResponse basicResponse = new DataResponse();

        try {
            // id와 userPassword로 authentication 토큰 생성
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(loginDTO.getId(), loginDTO.getPassword());

            //
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            //jwt token 발행
            String jwt = tokenProvider.createToken(authentication);

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);

            basicResponse = DataResponse.builder()
                    .code(200)
                    .httpStatus(HttpStatus.OK)
                    .message("로그인에 성공했습니다.")
                    .data(new TokenDTO(jwt))
                    .build();

        } catch (Exception e) {
            basicResponse = DataResponse.builder()
                    .code(401)
                    .httpStatus(HttpStatus.UNAUTHORIZED)
                    .message("비밀번호가 틀렸습니다.")
                    .build();
        }

        return new ResponseEntity<>(basicResponse, basicResponse.getHttpStatus());

//        return new ResponseEntity<>(new TokenDTO("로그인 성공 " + jwt), httpHeaders, HttpStatus.OK);
    }

    @ApiOperation(value = "회원가입", notes = "사용자 정보를 입력받아 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입에 성공했습니다.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserEntity.class))}),
            @ApiResponse(responseCode = "408", description = "이미 가입되어 있는 이메일입니다.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserEntity.class))}),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 아이디입니다.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserEntity.class))}),
            @ApiResponse(responseCode = "404", description = "서버에 문제가 생겼습니다.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserEntity.class))})
    })
    @PostMapping("/join")
    public ResponseEntity<BasicResponse> join(@RequestBody UserDTO userDTO) {


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

        BasicResponse basicResponse = new BasicResponse();
        try {
            if (userService.isUserIdExists(userDTO.getId()) != null) { // 이미 존재하는 아이디
                basicResponse = BasicResponse.builder()
                        .code(409)
                        .httpStatus(HttpStatus.CONFLICT)
                        .message("이미 존재하는 아이디입니다.")
                        .build();
            } else if (userService.isUserEmailExists(userDTO.getEmail()) != null) {
                basicResponse = BasicResponse.builder()
                        .code(408)
                        .httpStatus(HttpStatus.CONFLICT)
                        .message("이미 가입되어 있는 이메일입니다.")
                        .build();
            } else {
                userService.join(userDTO);
                basicResponse = BasicResponse.builder()
                        .code(200)
                        .httpStatus(HttpStatus.OK)
                        .message("회원가입에 성공했습니다.")
                        .build();
            }
        } catch (Exception e) {
            basicResponse = BasicResponse.builder()
                    .code(500)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .message("서버에 에러가 발생했습니다." + e)
                    .build();
        }

        return new ResponseEntity<>(basicResponse, basicResponse.getHttpStatus());
    }
}
