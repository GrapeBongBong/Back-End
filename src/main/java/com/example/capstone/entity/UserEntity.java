package com.example.capstone.entity;

import com.example.capstone.dto.UserDTO;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "user_table")
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonAutoDetect
public class UserEntity {
    @Id // pk 지정
//    @JsonIgnore
    @Column(name = "Uid")
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "Uid") // auto_increment
    private Long Uid;

    @Column//(unique = true)
    @NonNull
    private String id;

    @Column
    @NonNull
    private String password;

    @Column
    @NonNull
    private String name;

    @Column(name="nick_name")
    @NonNull
    private String nickName;

    @Column
    @NonNull
    private String birth; // 생년월일 (19991013)

    @Column(unique = true) // unique 제약 조건 추가
    @NonNull
    private String email;

    @Column
    @NonNull
    private String phone_num; // 01011112222

    @Column
    @NonNull
    private String address;

    @Column
    @NonNull
    private boolean activated;

    @Column
    @NonNull
    private String gender;

    @Column
    @NonNull
    private Double temperature;

    @Column
    private String profile_img; // 기본값은 null

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_role",
            joinColumns = {@JoinColumn(name = "Uid", referencedColumnName = "Uid")},
            inverseJoinColumns = {@JoinColumn(name = "role_name", referencedColumnName = "role_name")})
    private Set<RoleEntity> roles;

    //댓글
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    @Column(name = "comments")
    private List<Comment> comments = new ArrayList<>();

    // 댓글 목록 조회
    public List<Comment> getComments() {
        return Collections.unmodifiableList(comments);
    }

    // 댓글 추가
    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setUser(this);
    }

    // 댓글 삭제
    public void deleteComment(Comment comment) {
        comments.remove(comment);
        comment.setUser(null);
    }

    // 비밀번호 암호화
    public UserEntity hashPassword(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(this.password);
        return this;
    }

    public static UserEntity toUserEntity(UserDTO userDTO, RoleEntity role) {
        UserEntity userEntity = new UserEntity();

        userEntity.setId(userDTO.getId());
        userEntity.setPassword(userDTO.getPassword());
        userEntity.setName(userDTO.getName());
        userEntity.setNickName(userDTO.getNickName());
        userEntity.setBirth(userDTO.getBirth());
        userEntity.setEmail(userDTO.getEmail());
        userEntity.setPhone_num(userDTO.getPhoneNum());
        // activated 세팅 추가하기
        userEntity.setAddress(userDTO.getAddress());
        userEntity.setProfile_img(null); // 기본값 null
        userEntity.setGender(userDTO.getGender());
        userEntity.setTemperature(36.5);
        userEntity.setRoles(Collections.singleton(role));

        System.out.println("userEntity.getRoles() = " + userEntity.getRoles().toString());

        return userEntity;
    }

}
