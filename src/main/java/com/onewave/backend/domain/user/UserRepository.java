package com.onewave.backend.domain.user;

import com.onewave.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByGoogleSub(String googleSub);
    Optional<User> findByEmail(String email);
}