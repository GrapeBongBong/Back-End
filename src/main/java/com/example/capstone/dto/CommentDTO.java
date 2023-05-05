package com.example.capstone.dto;

import com.example.capstone.entity.Comment;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CommentDTO {

    private String content; //댓글
    private Long userId;

    // Comment 엔티티를 CommentDTO로 매핑
    public static CommentDTO from(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setContent(comment.getContent());
        dto.setUserId(comment.getUser().getUid()); //// Comment 엔티티의 User 객체에서 id 값을 가져와서 설정
        return dto;
    }

}

