package com.example.capstone.entity;

import com.example.capstone.dto.PostDTO;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "post")
@Inheritance(strategy = InheritanceType.JOINED)
@ToString
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Pid")
    private Long pid;

    @Column(nullable = false)
    private String title;

    // 작성 날짜
    @Column(nullable = false)
    private String date;
    // 작성 날짜

    @Column(nullable = false)
    private String content;

    // 게시물에 첨부한 이미지
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.EAGER) // 게시물, 게시물 사진 간 일대다 매핑
    @JsonManagedReference // 해당 객체를 직렬화할 때 postImages를 다룸 (무한루프 방지)
    @Column(name = "post_images")
    @Size(max = 3)
    private List<PostImage> postImages = new ArrayList<>();
    // 게시물에 첨부한 이미지

    @Enumerated(EnumType.STRING)
    private PostType postType;

    // 작성자
    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩 : 필요한 시점에 쿼리 실행하여 로딩
    @JoinColumn(name = "Uid", referencedColumnName = "Uid")
    private UserEntity user;
    // 작성자

    // 댓글
    @OneToMany(mappedBy = "post")
    @Column(name = "comments")
    private List<Comment> comments = new ArrayList<>();

    // 댓글 등록
    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setPost(this);
    }

    // 댓글 삭제
    public void deleteComment(Comment comment) {
        comments.remove(comment);
        comment.setPost(null);
    }

    public static String formatDate(LocalDateTime localDateTime) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedTime = localDateTime.format(dateTimeFormatter);
        return formattedTime;
    }
}
