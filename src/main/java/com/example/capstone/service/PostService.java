package com.example.capstone.service;

import com.example.capstone.dto.ExchangePostDTO;
import com.example.capstone.entity.ExchangePost;
import com.example.capstone.repository.PostRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

@Service
public class PostService {
    private PostRepository postRepository;

    public void save(ExchangePostDTO exchangePostDTO, User user) {
        ExchangePost exchangePost = ExchangePost.toExchangePost(exchangePostDTO, user);
        postRepository.save(exchangePost);
    }
}
