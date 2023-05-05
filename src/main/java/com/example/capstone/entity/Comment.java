package com.example.capstone.entity;

import com.example.capstone.dto.CommentRequestDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import com.example.capstone.dto.CommentDTO;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Optional;

@Entity
@Getter
@Setter
@Table(name = "comment")
@ToString
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩 : 필요한 시점에 쿼리 실행하여 로딩
    @JoinColumn(name = "Pid")
    private Post post;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime date;

    @ManyToOne
    @JoinColumn(name = "Uid")
    private UserEntity user;

    public static Comment from(CommentRequestDTO requestDTO, Post post, UserEntity user) {
        Comment comment = new Comment();
        comment.setContent(requestDTO.getContent());
        comment.setPost(post);
        comment.setUser(user);
        return comment;
    }

    public void updateComment(String content) {
        this.content = content;
    }

}
