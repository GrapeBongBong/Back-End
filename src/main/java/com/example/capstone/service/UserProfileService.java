package com.example.capstone.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.capstone.dto.UserProfileDTO;
import com.example.capstone.entity.UserEntity;
import com.example.capstone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final AmazonS3 amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

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

    public String uploadProfileImage(MultipartFile multipartFile, UserEntity user) throws IOException {
        // 메타데이터 설정
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(multipartFile.getContentType());
        objectMetadata.setContentLength(multipartFile.getSize());

        // S3 bucket 디렉토리명 설정
        String fileName = multipartFile.getOriginalFilename();
        String uploadFileName = UUID.randomUUID() + "_" + fileName; // S3 에 저장할 파일명
        log.info("uploadFileName: {}", uploadFileName);
        amazonS3Client.putObject(bucket, "profiles/" + uploadFileName, multipartFile.getInputStream(), objectMetadata); // S3 에 파일 업로드
        String imageUrl = amazonS3Client.getUrl(bucket, "profiles/" + uploadFileName).toString();
        log.info("image 업로드 {}: ", imageUrl);

        // DB 에 url 저장
        user.setProfile_img(imageUrl);
        log.info("user {}", user);
        userRepository.save(user);

        return imageUrl;
    }
}
