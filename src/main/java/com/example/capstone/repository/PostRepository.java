package com.example.capstone.repository;

import com.example.capstone.entity.ExchangePost;
import com.example.capstone.entity.Post;
import com.example.capstone.entity.PostType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    Post findByPid(Long pid);

    // ExchangePost 타입의 Post 객체만 조회
    List<?> findByPostType(PostType postType);

}
