package com.example.capstone.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "user_table")
public class UserEntity {
    @Id // pk 지정
    @Column(unique = true)
//    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto_increment
    private String id;

    @Column
    @NonNull
    private String password;

    @Column
    @NonNull
    private String name;

    @Column
    @NonNull
    private String nick_name;

    @Column
    @NonNull
    private String birth; // 생년월일 (19991013)

    @Column(unique = true) // unique 제약 조건 추가
    @NonNull
    private String email;

    @Column
    @NonNull
    private String gender;

    @Column
    @NonNull
    private String phone_num; // 010-1111-1111

    @Column
    @NonNull
    private String address;

    @Column
    @NonNull
    private String org_id; // 기관 ID (100001)

    @Column
    @NonNull
    private String job; // 직업

    @Column
    @NonNull
    private String hobby; // 취미
}
