package com.example.capstone.repository;

import com.example.capstone.entity.Post;
import com.example.capstone.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findById(String pid);
}
