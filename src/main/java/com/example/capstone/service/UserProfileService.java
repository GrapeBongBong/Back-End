package com.example.capstone.service;

import com.example.capstone.dto.UserProfileDTO;
import com.example.capstone.entity.UserEntity;
import com.example.capstone.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
public class UserProfileService {

    private final UserRepository userRepository;
    public UserProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserProfileDTO updateUserProfile(String userId, UserProfileDTO userProfileDTO) {
        Optional<UserEntity> userOptional = userRepository.findById(userId);

        if(userOptional.isEmpty()) {
            System.out.println("User not found");
        }

        UserEntity user = userOptional.get();

        if (userProfileDTO.getNickName() != null) {
            user.setNickName(userProfileDTO.getNickName());
        }
        if (userProfileDTO.getAddress() != null) {
            user.setAddress(userProfileDTO.getAddress());
        }

        UserEntity updatedUser = userRepository.save(user);

        return new UserProfileDTO(updatedUser.getNickName(), updatedUser.getAddress());

    }
}
