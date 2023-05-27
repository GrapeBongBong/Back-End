package com.example.capstone.repository;

import com.example.capstone.entity.ExchangePost;
import com.example.capstone.entity.Post;
import com.example.capstone.entity.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    // post 상세조회
    Post findByPid(Long pid);

    // PostType 구분해서 조회
    List<Post> findByPostType(PostType postType);

    Page<ExchangePost> findByPostType(PostType postType, Pageable pageable);

    @EntityGraph(attributePaths = "user")
    List<Post> findByUserIdAndPostType(String user_id, PostType postType);
}
