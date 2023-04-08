package com.example.capstone.repository;

import com.example.capstone.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    // 이메일로 회원정보 조회 (select * from user_table where id=?)
    // Optional: null 방지
//    Optional<UserEntity> findByUserEmail(String email);
    Optional<UserEntity> findById(String id);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findOneWithRolesById(String id);

    // 쿼리가 수행될 때 Lazy 조회가 아니고 Eager 조회로 authorities 정보를 같이 가져오게 된다.
//    @EntityGraph(attributePaths = "authorities")
    // User 정보 가져올 때 권한 정보도 같이 가져옴
//    Optional<UserEntity> findOneWithAuthoritiesById(String id);
}
