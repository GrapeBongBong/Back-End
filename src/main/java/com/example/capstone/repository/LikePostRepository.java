package com.example.capstone.repository;

import com.example.capstone.entity.LikePost;
import com.example.capstone.entity.Post;
import com.example.capstone.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikePostRepository extends JpaRepository<LikePost, Long> {
    boolean existsByUserAndPost(UserEntity user, Post post);
}
