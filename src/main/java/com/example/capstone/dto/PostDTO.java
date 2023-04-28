package com.example.capstone.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {
    private Long pid; // 게시글 아이디
    private String title; // 게시글 제목
    private String content; // 게시글 내용
    private String date; // 게시글 작성 날짜
    // 첨부 이미지
    private String writerNick; // 게시글 작성자 닉네임
    private String writerId; // 게시글 작성자 아이디
}
