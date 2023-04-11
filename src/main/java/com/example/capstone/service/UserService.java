package com.example.capstone.service;

import com.example.capstone.dto.UserDTO;
import com.example.capstone.entity.UserEntity;
import com.example.capstone.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder bCryptPasswordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    // 아이디가 존재하는지 체크
    public UserEntity isUserIdExists(String userId) {
        return userRepository.findById(userId).orElse(null);
    }

    // 가입되어 있는 이메일인지 체크
    public UserEntity isUserEmailExists(String userEmail) {
        return userRepository.findByEmail(userEmail).orElse(null);
    }

    public void join(UserDTO userDTO) {

       /* Authority authority = Authority.builder()
                .authorityName("ROLE_USER")
                .build();*/

        UserEntity newUser = UserEntity.builder()
                .id(userDTO.getId())
                .password(userDTO.getPassword())
                .name(userDTO.getName())
                .nick_name(userDTO.getNickName())
                .birth(userDTO.getBirth())
                .email(userDTO.getEmail())
                .phone_num(userDTO.getPhoneNum())
                .address(userDTO.getAddress())
                .talent(userDTO.getTalent())
                .profile_img(userDTO.getProfile_img())
                //.authorities(Collections.singleton(authority))
                .build();

        // UserEntity 객체의 authorities 필드에 권한 정보 저장
//        newUser.setAuthorities(Collections.singleton(authority));

        // 사용자 비밀번호 암호화
        newUser.hashPassword(bCryptPasswordEncoder);

        // repository 의 save() 호출 (entity 객체 넘겨줘야 함)
        userRepository.save(newUser);
        //System.out.println(newUser.getAuthorities());
    }
}