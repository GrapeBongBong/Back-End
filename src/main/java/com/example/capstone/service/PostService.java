package com.example.capstone.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.capstone.data.AvailableTime;
import com.example.capstone.dto.AnonymousPostDTO;
import com.example.capstone.dto.ExchangePostDTO;
import com.example.capstone.dto.PostDTO;
import com.example.capstone.entity.*;
import com.example.capstone.repository.PostRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;

import static com.example.capstone.entity.ExchangePost.formatDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
    private final PostRepository postRepository;

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public void save(PostDTO postDTO, List<MultipartFile> imageFiles, UserEntity userEntity) throws IOException {

        Post completedPost = new Post();
        List<PostImage> postImages = new ArrayList<>();

        System.out.println("PostDTO = " + postDTO.toString());

        if (postDTO.getPostType() == PostType.T) { // 재능교환 게시물일 경우 ExchangePost 로 저장
            completedPost = ExchangePost.toExchangePost((ExchangePostDTO) postDTO);
        } else if (postDTO.getPostType() == PostType.A) { // 익명 커뮤니티 게시물일 경우 AnonymousPost 로 저장
            completedPost = AnonymousPost.toAnonymousPost((AnonymousPostDTO) postDTO);
        }

        if (imageFiles != null) { // 이미지 첨부한 경우
            saveImages(imageFiles, completedPost, postImages);
            completedPost.setPostImages(postImages);
        }
        completedPost.setUser(userEntity); // 받아온 사용자 정보를 이용해서 게시물 작성자 정보 저장

        postRepository.save(completedPost);
    }

    // MultipartFile 을 전달받아 File 로 전환한 후 S3 에 업로드
    private void saveImages(List<MultipartFile> imageFiles, Post completedPost, List<PostImage> postImages) throws IOException {

        for (MultipartFile imageFile: imageFiles) {
//            File uploadFile = convert(imageFile)
//                    .orElseThrow(() -> new IllegalArgumentException("MultipartFile -> File 전환 실패"));
            String fileName = imageFile.getOriginalFilename();
            String fileUrl = "https://" + bucket + "/jeinie" + fileName;
            String uploadFileName = UUID.randomUUID() + "/" + fileName; // S3 에 저장할 파일명
            log.info("fileName: {}", fileName);
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(imageFile.getContentType());
            objectMetadata.setContentLength(imageFile.getSize());
            amazonS3Client.putObject(bucket, uploadFileName, imageFile.getInputStream(), objectMetadata); // S3 에 파일 업로드
            log.info("image 업로드 {}: ", amazonS3Client.getUrl(bucket, fileName).toString());
        }
    }

    // 파일 convert 후 로컬에 업로드
    public Optional<File> convert(MultipartFile imageFile) throws IOException {
        File convertFile = new File("/home/ec2-user/images/" + imageFile.getOriginalFilename());
        if (convertFile.createNewFile()) {
            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                fos.write(imageFile.getBytes());
            }
            return Optional.of(convertFile);
        }
        return Optional.empty();
    }

    public void delete(Post post) {
        postRepository.delete(post);
    }

    public void update(PostDTO postDTO, Post post) {
        if (post.getPostType() == PostType.T) { // 재능 게시물인 경우
            ExchangePost exchangePost = (ExchangePost) post;
            ExchangePostDTO exchangePostDTO = (ExchangePostDTO) postDTO;
            exchangePost.setTitle(exchangePostDTO.getTitle());
            exchangePost.setDate(formatDate(LocalDateTime.now())); // yyyy-MM-dd HH:mm:ss
            exchangePost.setContent(exchangePostDTO.getContent());
            exchangePost.setGiveCate(exchangePostDTO.getGiveCate());
            exchangePost.setGiveTalent(exchangePostDTO.getGiveTalent());
            exchangePost.setTakeCate(exchangePostDTO.getTakeCate());
            exchangePost.setTakeTalent(exchangePostDTO.getTakeTalent());

            // 시간대 정보 저장
            AvailableTime availableTime = exchangePostDTO.getAvailableTime();
            exchangePost.setDays(availableTime.getDays());
            exchangePost.setTimezone(availableTime.getTimezone());

        } else if (post.getPostType() == PostType.A) { // 익명 게시물인 경우
            AnonymousPost anonymousPost = (AnonymousPost) post;
            AnonymousPostDTO anonymousPostDTO = (AnonymousPostDTO) postDTO;
            anonymousPost.setTitle(anonymousPostDTO.getTitle());
            anonymousPost.setDate(formatDate(LocalDateTime.now()));
            anonymousPost.setContent(anonymousPostDTO.getContent());
        }
    }

/*    public Page<Post> getAllPosts(Pageable pageable) {
        List<Post> posts = postRepository.findAll(); // DB에서 데이터를 가져오는 코드

        int total = posts.size();
        int start = pageable.getPageNumber() * pageable.getPageSize();
        int end = Math.min(start + pageable.getPageSize(), total);

        if (start > end) {
            return new PageImpl<>(Collections.emptyList(), pageable, total);
        } else {
            return new PageImpl<>(posts.subList(start, end), pageable, total);
        }
    }

    public long getPostCount() {
        return postRepository.count();
    }*/

}
