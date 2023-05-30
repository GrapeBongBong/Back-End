package com.example.capstone.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter

@Entity
@Table(name = "rating")
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 평점 테이블
    // match 에서 가져와야 할 듯?
    @ManyToOne(fetch = FetchType.LAZY)
    private Match match;

    // 평점
    private int score;
}
