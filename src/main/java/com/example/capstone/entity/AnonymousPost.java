package com.example.capstone.entity;

import com.example.capstone.dto.AnonymousPostDTO;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "anonymous_post")
public class AnonymousPost extends Post {

    public static AnonymousPost toAnonymousPost(AnonymousPostDTO anonymousPostDTO) { // dto -> entity

        AnonymousPost anonymousPost = new AnonymousPost();
        anonymousPost.setTitle(anonymousPostDTO.getTitle());
        anonymousPost.setDate(formatDate(LocalDateTime.now()));
        anonymousPost.setContent(anonymousPostDTO.getContent());
        anonymousPost.setPostType(anonymousPostDTO.getPostType());

        // 이미지 관련 로직 추가

        return anonymousPost;
    }
}
