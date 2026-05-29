package com.code.rank.repository;

import com.code.rank.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByRole(com.code.rank.entity.Role role);
    Optional<User> findFirstByRoleOrderByIdAsc(com.code.rank.entity.Role role);
}
