package com.lms.backend.repository;

import com.lms.backend.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    // This method is used by JwtAuthFilter, AuthService, and CustomUserDetailsService
    Optional<AppUser> findByEmailIgnoreCase(String email);
    Optional<AppUser> findByResetPasswordToken(String token);
}