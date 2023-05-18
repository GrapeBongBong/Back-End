package com.example.capstone.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.capstone.data.AvailableTime;
import com.example.capstone.dto.AnonymousPostDTO;
import com.example.capstone.dto.ExchangePostDTO;
import com.example.capstone.dto.PostDTO;
import com.example.capstone.entity.*;
import com.example.capstone.repository.PostImageRepository;
import com.example.capstone.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static com.example.capstone.entity.ExchangePost.formatDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final AmazonS3 amazonS3Client;

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
            List<String> imageUrls = new ArrayList<>();
            imageUrls = saveImages(imageFiles);
            log.info("imageUrls: {}", imageUrls);
            for (String imageUrl: imageUrls) {
                PostImage postImage = new PostImage();
                postImage.setPost(completedPost);
                postImage.setFileUrl(imageUrl);
                postImages.add(postImage);
            }
            completedPost.setPostImages(postImages);
        }
        completedPost.setUser(userEntity); // 받아온 사용자 정보를 이용해서 게시물 작성자 정보 저장

        postRepository.save(completedPost);
    }

    // MultipartFile 을 전달받아 File 로 전환한 후 S3 에 업로드
    private List<String> saveImages(List<MultipartFile> imageFiles) throws IOException {

        List<String> imageUrls = new ArrayList<>();

        for (MultipartFile imageFile: imageFiles) {
            // 메타데이터 설정
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(imageFile.getContentType());
            objectMetadata.setContentLength(imageFile.getSize());

            // S3 bucket 디렉토리명 설정
            String fileName = imageFile.getOriginalFilename();
            String uploadFileName = UUID.randomUUID() + "_" + fileName; // S3 에 저장할 파일명
            log.info("uploadFileName: {}", uploadFileName);
            amazonS3Client.putObject(bucket, "images/" + uploadFileName, imageFile.getInputStream(), objectMetadata); // S3 에 파일 업로드
            String imageUrl = amazonS3Client.getUrl(bucket, "images/" + uploadFileName).toString();
            log.info("image 업로드 {}: ", imageUrl);

            imageUrls.add(imageUrl);
        }
        return imageUrls;
    }

    public String delete(Post post) {
        // 해당 포스트에 이미지 있는지 확인
        List<PostImage> postImages = postImageRepository.findPostImagesByPost(post);

        if (postImages != null) { // 이미지가 있다면 S3 에서 삭제
            for (PostImage postImage: postImages) {
                String imageUrl = postImage.getFileUrl();
                String imageFileName = imageUrl.substring(imageUrl.indexOf("images/"));
                log.info("imageFileName {}", imageFileName);
                boolean isObjectExist = amazonS3Client.doesObjectExist(bucket, imageFileName); // S3 에 해당 이미지 있는지 확인
                if (isObjectExist) { // S3 에 해당 이미지가 저장되어 있는 경우
                    amazonS3Client.deleteObject(bucket, imageFileName);
                } else {
                    return "S3 에 저장되어 있지 않은 이미지가 있습니다.";
                }
            }
        }
        postRepository.delete(post);

        return "게시글을 성공적으로 삭제했습니다.";
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
