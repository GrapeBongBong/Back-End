package com.example.capstone.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String password;
    private String name;
    private String nickName;
    private String birth; // 생년월일
    private String email;
    private String gender;
    private String phoneNum;
    private String address;
    private String org_id; // 기관 ID
    private String job; // 직업
    private String hobby; // 취미
}
