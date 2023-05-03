package com.example.capstone.service;

import com.example.capstone.data.AvailableTime;
import com.example.capstone.dto.AnonymousPostDTO;
import com.example.capstone.dto.ExchangePostDTO;
import com.example.capstone.dto.PostDTO;
import com.example.capstone.entity.*;
import com.example.capstone.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.example.capstone.entity.ExchangePost.formatDate;

@Service
public class PostService {
    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public void save(PostDTO postDTO, UserEntity userEntity) {

        Post completedPost = new Post();
        System.out.println("PostDTO = " + postDTO.toString());

        if (postDTO.getPostType() == PostType.T) { // 재능교환 게시물일 경우 ExchangePost 로 저장
            completedPost = ExchangePost.toExchangePost((ExchangePostDTO) postDTO);
            completedPost.setUser(userEntity); // 받아온 사용자 정보를 이용해서 게시물 작성자 정보 저장

        } else if (postDTO.getPostType() == PostType.A) { // 익명 커뮤니티 게시물일 경우 AnonymousPost 로 저장
            completedPost = AnonymousPost.toAnonymousPost((AnonymousPostDTO) postDTO);
            completedPost.setUser(userEntity);
        }

        postRepository.save(completedPost);
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
}
