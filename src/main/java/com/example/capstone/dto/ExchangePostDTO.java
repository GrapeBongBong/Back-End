package com.example.capstone.dto;

import com.example.capstone.data.AvailableTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExchangePostDTO {
    @NotBlank
    private String title; // 게시글 제목

    @NotBlank
    private String content; // 게시글 내용

    @NotBlank
    private String giveCate; // 주는 카테고리

    @NotBlank
    private String giveTalent; // 주는 재능

    @NotBlank
    private String takeCate; // 받는 카테고리

    @NotBlank
    private String takeTalent; // 받는 재능

    private AvailableTime availableTime; // 가능한 시간대

    // 이미지 필드 추가
}