package com.example.capstone.controller;

import com.example.capstone.dto.UserDTO;
import com.example.capstone.data.BasicResponse;
import com.example.capstone.entity.UserEntity;
import com.example.capstone.repository.UserRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api(tags = {"로그인 / 회원가입 API"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;

    @ApiOperation(value = "hello 매소드", notes = "hello 메시지를 반환합니다.") // hello() 메소드 문서화
    @PostMapping("/login")
    public String login() {
        return "hello";
    }

    @ApiOperation(value = "회원가입", notes = "사용자 정보를 입력받아 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "회원가입에 성공했습니다."),
            @ApiResponse(code = 404, message = "서버에 문제가 생겼습니다.")
    })
    @PostMapping("/join")
    public ResponseEntity<BasicResponse> join(@RequestBody UserDTO userDTO) {

        /**
         * {
         *     "user_id": "1111",
         *     "password": "1111",
         *     "name": "홍길동",
         *     "nickName": "홍길동",
         *     "birth": "991013",
         *     "email": "aaaa@naver.com",
         *     "gender": "여자",
         *     "phoneNum": "010-1111-1111",
         *     "address": "서울시 성북구",
         *     "org_id": "100001",
         *     "job": "학생",
         *     "hobby": "영화"
         * }
         */
        System.out.println("AuthController.join");

        BasicResponse basicResponse = new BasicResponse();

        try {
            UserEntity userEntity = new UserEntity();
            userEntity.setId(userDTO.getId());
            userEntity.setPassword(userDTO.getPassword());
            userEntity.setName(userDTO.getName());
            userEntity.setNick_name(userDTO.getNickName());
            userEntity.setBirth(userDTO.getBirth());
            userEntity.setEmail(userDTO.getEmail());
            userEntity.setGender(userDTO.getGender());
            userEntity.setPhone_num(userDTO.getPhoneNum());
            userEntity.setAddress(userDTO.getAddress());
            userEntity.setOrg_id(userDTO.getOrg_id());
            userEntity.setJob(userDTO.getJob());
            userEntity.setHobby(userDTO.getHobby());

            // repository 의 save() 호출 (entity 객체 넘겨줘야 함)
            userRepository.save(userEntity);

            basicResponse = BasicResponse.builder()
                    .code(200)
                    .httpStatus(HttpStatus.OK)
                    .message("회원가입에 성공했습니다.")
                    .build();
        } catch (Exception e) {
            basicResponse = BasicResponse.builder()
                    .code(404)
                    .httpStatus(HttpStatus.NOT_FOUND)
                    .message("서버에 에러가 발생했습니다." + e)
                    .build();
        }

        // 정상적으로 entity 가 담겨지고 save() 가 호출되었으면 성공
        // 그렇지 않으면 실패

        return new ResponseEntity<>(basicResponse, basicResponse.getHttpStatus());
    }
}
