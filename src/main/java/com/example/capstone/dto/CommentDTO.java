package com.example.capstone.dto;

import com.example.capstone.entity.Comment;
import com.example.capstone.entity.Post;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CommentDTO { //댓글 정보 전송시 사용

    private Long commentId; //댓글 ID
    private Long postId; //게시물 ID
    private String content; //댓글 내용
    private String date; //댓글 작성일자
    private Long userId; //사용자 ID


    // Comment 엔티티를 CommentDTO로 매핑
    public static CommentDTO tocommentDTO(Comment comment) {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setCommentId(comment.getCommentId());
        commentDTO.setPostId(comment.getPost().getPid());
        commentDTO.setContent(comment.getContent());
        commentDTO.setDate(formatDate(comment.getDate()));
        commentDTO.setUserId(comment.getUser().getUid());
        return commentDTO;
    }

    public static String formatDate(LocalDateTime localDateTime) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedTime = localDateTime.format(dateTimeFormatter);
        return formattedTime;
    }
}

