package com.example.capstone.repository;

import com.example.capstone.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    Post findByPid(Long pid);
}
