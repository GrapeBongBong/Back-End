package com.example.capstone.service;

import com.example.capstone.dto.UserDTO;
import com.example.capstone.entity.RoleEntity;
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

        RoleEntity role = RoleEntity.builder()
                .roleName("ROLE_USER")
                .build();

        UserEntity newUser = UserEntity.toUserEntity(userDTO, role);

        // 사용자 비밀번호 암호화
        newUser.hashPassword(bCryptPasswordEncoder);

        // repository 의 save() 호출 (entity 객체 넘겨줘야 함)
        userRepository.save(newUser);
        System.out.println("newUser.getAuthorities = " + newUser.getRoles().toString());
        System.out.println("newUser = " + newUser.toString());

    }
}