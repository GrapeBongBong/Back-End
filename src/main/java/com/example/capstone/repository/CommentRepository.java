package com.example.capstone.repository;

import com.example.capstone.entity.Comment;
import com.example.capstone.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment,Long> {
    List<Comment> findByPost(Post post);
    List<Comment> findByPostPid(Long postId);
}

