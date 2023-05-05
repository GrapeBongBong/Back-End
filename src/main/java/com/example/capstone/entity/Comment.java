package com.example.capstone.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import com.example.capstone.dto.CommentDTO;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "comment")
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

    // Post와 양방향 연관관계 설정
    public void setPost(Post post) {
        this.post = post;
        post.getComments().add(this);
    }

    // UserEntity와 양방향 연관관계 설정
    public void setUser(UserEntity user) {
        this.user = user;
        user.getComments().add(this);
    }

    // CommentDTO를 Comment 엔티티로 매핑
    public static Comment toEntity(CommentDTO commentDTO) {
        Comment comment = new Comment();
        comment.setContent(commentDTO.getContent());
        return comment;
    }


}
