package com.example.capstone.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "post_image")
public class PostImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "p_img_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Pid")
    @JsonBackReference // 해당 객체를 직렬화할 때 post 는 무시 (무한루프 방지)
    private Post post;

    /*@Lob
    private byte[] image;*/

    @Column(name = "fileUrl", nullable = false)
    private String fileUrl;

}
