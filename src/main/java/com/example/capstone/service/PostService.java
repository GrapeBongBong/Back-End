package com.example.capstone.service;

import com.example.capstone.data.AvailableTime;
import com.example.capstone.dto.AnonymousPostDTO;
import com.example.capstone.dto.ExchangePostDTO;
import com.example.capstone.dto.PostDTO;
import com.example.capstone.entity.*;
import com.example.capstone.repository.PostRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.example.capstone.entity.ExchangePost.formatDate;

@Service
@AllArgsConstructor
@Slf4j
public class PostService {
    private final PostRepository postRepository;
    private final Environment environment;

    public List<String> save(PostDTO postDTO, List<MultipartFile> imageFiles, UserEntity userEntity) throws IOException {

        Post completedPost = new Post();
        List<PostImage> postImages = new ArrayList<>();
        List<String> imageUrls = new ArrayList<>();

        System.out.println("PostDTO = " + postDTO.toString());

        if (postDTO.getPostType() == PostType.T) { // 재능교환 게시물일 경우 ExchangePost 로 저장
            completedPost = ExchangePost.toExchangePost((ExchangePostDTO) postDTO);
        } else if (postDTO.getPostType() == PostType.A) { // 익명 커뮤니티 게시물일 경우 AnonymousPost 로 저장
            completedPost = AnonymousPost.toAnonymousPost((AnonymousPostDTO) postDTO);
        }

        if (imageFiles != null) { // 이미지 첨부한 경우
            imageUrls = saveImages(imageFiles, completedPost, postImages);
            completedPost.setPostImages(postImages);
        }
        completedPost.setUser(userEntity); // 받아온 사용자 정보를 이용해서 게시물 작성자 정보 저장

        postRepository.save(completedPost);

        return imageUrls;
    }

    private List<String> saveImages(List<MultipartFile> imageFiles, Post completedPost, List<PostImage> postImages) throws IOException {
        List<String> imageUrls = new ArrayList<>();

        for (MultipartFile imageFile: imageFiles) {
            PostImage image = new PostImage();
            String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename(); // 파일 이름 생성
            String imageStorageLocation = environment.getProperty("app.image.storage.location");
            File destinationFile = new File(imageStorageLocation + "/" + fileName);
            imageFile.transferTo(destinationFile);
//            String filePath = ResourceUtils.getFile("classpath:images/").getPath() + "/" + fileName;
//            imageFile.transferTo(new File(filePath));
            log.info("filePath = " + imageStorageLocation);

            image.setPost(completedPost);
            image.setFileName(fileName);
            image.setFileOriginName(imageFile.getOriginalFilename());
            image.setFileUrl(imageStorageLocation);
//            image.setImage(imageFile.getBytes());
            postImages.add(image);
            imageUrls.add(imageStorageLocation);
        }

        return imageUrls;
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
