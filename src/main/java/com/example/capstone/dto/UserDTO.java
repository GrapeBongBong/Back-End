package com.example.capstone.dto;

import com.example.capstone.entity.RoleEntity;
import com.example.capstone.entity.UserEntity;
import lombok.*;
import org.springframework.security.core.userdetails.User;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String id; // 사용자 아이디
    private String password;
    private String name;
    private String nickName;
    private String birth; // 생년월일
    private String email;
    private String gender;
    private String phoneNum;
    private String address;
//    private String profile_img; // 프로필 이미지 url
}
