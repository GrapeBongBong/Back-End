package com.example.capstone.entity;

import com.example.capstone.dto.UserDTO;
import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "user_table")
public class UserEntity {
    @Id // pk 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto_increment
    private Long id;

    @Column
    private String password;

    @Column
    private String name;

    @Column
    private String nickName;

    @Column
    private String birth; // 생년월일 (19991013)

    @Column(unique = true) // unique 제약 조건 추가
    private String email;

    @Column
    private String gender;

    @Column
    private String phoneNum; // 010-1111-1111

    @Column
    private String address;

    @Column
    private String org_id; // 기관 ID (100001)

    @Column
    private String job; // 직업

    @Column
    private String hobby; // 취미
}
