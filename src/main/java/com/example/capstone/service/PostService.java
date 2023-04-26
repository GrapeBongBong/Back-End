package com.example.capstone.service;

import com.example.capstone.data.AvailableTime;
import com.example.capstone.dto.ExchangePostDTO;
import com.example.capstone.entity.ExchangePost;
import com.example.capstone.entity.UserEntity;
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

    public void save(ExchangePostDTO exchangePostDTO, UserEntity userEntity) {
        ExchangePost exchangePost = ExchangePost.toExchangePost(exchangePostDTO);
        exchangePost.setUser(userEntity); // 받아온 사용자 정보를 이용해서 게시물 작성자 정보 저장

        postRepository.save(exchangePost);
    }

    public void delete(ExchangePost exchangePost) {
        postRepository.delete(exchangePost);
    }

    public void update(ExchangePostDTO exchangePostDTO, ExchangePost exchangePost) {
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
    }
}
