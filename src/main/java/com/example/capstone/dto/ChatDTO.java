package com.example.capstone.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatDTO {
    private Long exchangePostId; // 재능교환 게시글 아이디
    private String applicantId; // 신청자 아이디
}
