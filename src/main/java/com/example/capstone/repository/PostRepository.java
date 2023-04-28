package com.example.capstone.repository;

import com.example.capstone.entity.ExchangePost;
import com.example.capstone.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    Post findByPid(Long pid);

    @Query("SELECT p, e from Post p left join ExchangePost e on p.pid = e.pid")
    List<ExchangePost[]> findPosts();
}
