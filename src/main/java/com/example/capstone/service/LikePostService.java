package com.example.capstone.service;

import com.example.capstone.entity.LikePost;
import com.example.capstone.entity.Post;
import com.example.capstone.entity.UserEntity;
import com.example.capstone.repository.LikePostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikePostService {
    private final LikePostRepository likePostRepository;

    public void likePostByUser(UserEntity user, Post post) {
        LikePost likePost = new LikePost();
        likePost.setUser(user);
        likePost.setPost(post);

        likePostRepository.save(likePost);
    }

    public boolean isLiked(UserEntity user, Post post) { // 좋아요를 누른 게시글인지 체크
        return likePostRepository.existsByUserAndPost(user, post);
    }
}
