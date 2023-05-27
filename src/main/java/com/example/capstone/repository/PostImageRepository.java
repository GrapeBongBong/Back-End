package com.example.capstone.repository;

import com.example.capstone.entity.Post;
import com.example.capstone.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {
    List<PostImage> findPostImagesByPost(Post post);
}
