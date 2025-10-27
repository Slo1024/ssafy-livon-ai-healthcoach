package com.s406.livon.domain.user.repository;

import com.s406.livon.domain.user.entity.User;
import com.s406.livon.domain.user.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
//  Optional<User> findById(String username);

  boolean existsByEmail(String username);
  boolean existsByNickname(String nickname);

  Optional<User> findByEmail(String email);
    /**
     * ID로 특정 역할을 가진 사용자 조회
     * User의 roles가 List이므로 커스텀 쿼리 사용
     */
    @Query("SELECT u FROM User u WHERE u.id = :id AND :role MEMBER OF u.roles")
    Optional<User> findByIdAndRole(@Param("id") UUID id, @Param("role") Role role);
}