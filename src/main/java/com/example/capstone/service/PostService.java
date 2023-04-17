package com.example.capstone.service;

import com.example.capstone.dto.ExchangePostDTO;
import com.example.capstone.entity.ExchangePost;
import com.example.capstone.entity.Post;
import com.example.capstone.entity.UserEntity;
import com.example.capstone.repository.PostRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class PostService {
    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public void save(ExchangePostDTO exchangePostDTO, UserEntity userEntity) {
        ExchangePost exchangePost = ExchangePost.toExchangePost(exchangePostDTO);
        // 받아온 사용자 정보를 이용해서 게시물의 작성자, 닉네임 등 설정
//        exchangePost.setUser(userEntity);
        postRepository.save(exchangePost);
    }
}
