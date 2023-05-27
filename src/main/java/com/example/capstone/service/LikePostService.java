package com.example.capstone.service;

import com.example.capstone.entity.*;
import com.example.capstone.repository.LikePostRepository;
import com.example.capstone.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    public List<Boolean> getIsLiked(UserEntity user, List<Post> postList) {
        List<Boolean> isLiked = new ArrayList<>();

        for (Post post: postList) {
            isLiked.add(likePostRepository.existsByUserAndPost(user, post));
        }

        return isLiked;
    }
}
