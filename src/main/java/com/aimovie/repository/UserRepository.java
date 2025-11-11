package com.aimovie.repository;

import com.aimovie.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByUsernameOrEmail(String username, String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    long countByEnabledTrue();
    long countByEnabledFalse();
}



