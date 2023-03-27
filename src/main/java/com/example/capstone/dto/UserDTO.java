package com.example.capstone.dto;

import com.example.capstone.entity.RoleEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long uid;
    private String id; // 사용자 아이디
    private String password;
    private String name;
    private String nickName;
    private String birth; // 생년월일
    private String email;
    private String gender;
    private String phoneNum;
    private String address;
    private String talent; // 재능
    private String profile_img; // 프로필 이미지 url
    private Set<RoleEntity> role;
}
