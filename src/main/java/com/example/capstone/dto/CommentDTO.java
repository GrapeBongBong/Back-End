package com.example.capstone.dto;

import com.example.capstone.entity.Comment;
import com.example.capstone.entity.Post;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CommentDTO { //댓글 정보 전송시 사용

    private Long commentId;
    private Long postId;
    private String content;
    private LocalDateTime date;
    private Long userId;


    // Comment 엔티티를 CommentDTO로 매핑
    public static CommentDTO tocommentDTO(Comment comment) {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setCommentId(comment.getCommentId());
        commentDTO.setPostId(comment.getPost().getPid());
        commentDTO.setContent(comment.getContent());
        commentDTO.setDate(comment.getDate());
        commentDTO.setUserId(comment.getUser().getUid());
        return commentDTO;
    }



}

