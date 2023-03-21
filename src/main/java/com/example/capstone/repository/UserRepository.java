package com.example.capstone.repository;

import com.example.capstone.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    // 이메일로 회원정보 조회 (select * from user_table where id=?)
    // Optional: null 방지
//    Optional<UserEntity> findByUserEmail(String email);
    Optional<UserEntity> findById(String id);
}
