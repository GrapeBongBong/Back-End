package com.example.capstone.dto;

import com.example.capstone.entity.PostType;
import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PostDTO {
    @NotBlank
    private String title; // 게시글 제목

    @NotBlank
    private String content; // 게시글 내용
    
    // 첨부 이미지

    private String writerNick; // 게시글 작성자 닉네임
    private String writerId; // 게시글 작성자 아이디
    private PostType postType; // 포스트 타입
}
