package com.example.capstone.controller;

import com.example.capstone.dto.UserDTO;
import com.example.capstone.entity.UserEntity;
import com.example.capstone.repository.UserRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Api(tags = {"API 정보를 제공하는 Controller"})
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

    @PostMapping("/join")
    public void join(@RequestBody UserDTO userDTO) {
        /**
         * {
         *     "id": "1111",
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

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userDTO.getId());
        userEntity.setPassword(userDTO.getPassword());
        userEntity.setName(userDTO.getName());
        userEntity.setNickName(userDTO.getNickName());
        userEntity.setBirth(userDTO.getBirth());
        userEntity.setEmail(userDTO.getEmail());
        userEntity.setGender(userDTO.getGender());
        userEntity.setPhoneNum(userDTO.getPhoneNum());
        userEntity.setAddress(userDTO.getAddress());
        userEntity.setOrg_id(userDTO.getOrg_id());
        userEntity.setJob(userDTO.getJob());
        userEntity.setHobby(userDTO.getHobby());

        // repository 의 save() 호출 (entity 객체 넘겨줘야 함)
        userRepository.save(userEntity);
    }
}
