package com.example.capstone.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter

@Entity
@Table(name = "rating")
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 평점 테이블
    @ManyToOne(fetch = FetchType.LAZY)
    private Match match;

    // 평점을 매긴 사용자 uid
    private Long uid;

    // 평점
    private int score;
}
