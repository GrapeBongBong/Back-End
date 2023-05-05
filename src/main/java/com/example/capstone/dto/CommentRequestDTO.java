package com.example.capstone.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CommentRequestDTO { //댓글 등록시 요청에 필요한 데이터

    private String content; //댓글

}
