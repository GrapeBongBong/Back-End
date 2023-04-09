package com.example.capstone.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExchangePostDTO {
    private Long Uid; // 작성자 uid
    private String title; // 게시글 제목
    private String content; // 게시글 내용
    private String giveCate; // 주는 카테고리
    private String giveTalent; // 주는 재능
    private String takeCate; // 받는 카테고리
    private String takeTalent; // 받는 재능
    // 이미지 필드 추가
}