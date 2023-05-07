package com.example.capstone.service;

import com.example.capstone.data.AvailableTime;
import com.example.capstone.dto.AnonymousPostDTO;
import com.example.capstone.dto.ExchangePostDTO;
import com.example.capstone.dto.PostDTO;
import com.example.capstone.entity.*;
import com.example.capstone.repository.PostRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.example.capstone.dto.PostDTO;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.example.capstone.entity.ExchangePost.formatDate;

@Service
@AllArgsConstructor
public class PostService {
    private final PostRepository postRepository;

    public void save(PostDTO postDTO, List<MultipartFile> imageFiles, UserEntity userEntity) throws IOException {

        Post completedPost = new Post();
        List<PostImage> postImages = new ArrayList<>();

        System.out.println("PostDTO = " + postDTO.toString());

        System.out.println("imageFiles = " + imageFiles.get(0).getOriginalFilename());

        if (postDTO.getPostType() == PostType.T) { // 재능교환 게시물일 경우 ExchangePost 로 저장
            completedPost = ExchangePost.toExchangePost((ExchangePostDTO) postDTO);
            if (imageFiles != null) { // 이미지 첨부한 경우
                saveImages(imageFiles, completedPost, postImages);
                completedPost.setPostImages(postImages);
            }
            completedPost.setUser(userEntity); // 받아온 사용자 정보를 이용해서 게시물 작성자 정보 저장

        } else if (postDTO.getPostType() == PostType.A) { // 익명 커뮤니티 게시물일 경우 AnonymousPost 로 저장
            completedPost = AnonymousPost.toAnonymousPost((AnonymousPostDTO) postDTO);
            if (imageFiles != null) { // 이미지 첨부한 경우
                saveImages(imageFiles, completedPost, postImages);
                completedPost.setPostImages(postImages);
            }
            completedPost.setUser(userEntity);
        }

        postRepository.save(completedPost);
    }

    private void saveImages(List<MultipartFile> imageFiles, Post completedPost, List<PostImage> postImages) throws IOException {
        for (MultipartFile imageFile: imageFiles) {
            PostImage image = new PostImage();
            image.setPost(completedPost);
            image.setImage(imageFile.getBytes());
            postImages.add(image);
        }
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
            // 이미지 세팅 추가하기

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
