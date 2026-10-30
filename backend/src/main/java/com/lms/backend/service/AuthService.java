package com.lms.backend.service;

import com.lms.backend.entity.AppUser;
import com.lms.backend.entity.Role;
import com.lms.backend.exception.DomainExceptions.BusinessRuleException;
import com.lms.backend.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AppUser register(String email, String password, String firstName, String lastName) {
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new BusinessRuleException("Email already in use.");
        }

        AppUser user = AppUser.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .role(Role.MEMBER) // Default role
                .createdAt(Instant.now())
                .build();

        return userRepository.save(user);
    }

    // This method is needed by AuthController for the login flow
    public AppUser findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public void createPasswordResetToken(String email) {
        AppUser user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessRuleException("User not found"));

        String token = UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        user.setResetPasswordTokenExpiry(Instant.now().plus(15, ChronoUnit.MINUTES));
        userRepository.save(user);

        // In a real app, you would send an email here using JavaMailSender.
        // For this tutorial, we print it to the console.
        System.out.println("=====================================================");
        System.out.println("PASSWORD RESET LINK FOR " + email);
        System.out.println("http://localhost:4200/reset-password?token=" + token);
        System.out.println("=====================================================");
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        AppUser user = userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new BusinessRuleException("Invalid password reset token"));

        if (user.getResetPasswordTokenExpiry().isBefore(Instant.now())) {
            throw new BusinessRuleException("Password reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
        userRepository.save(user);
    }
}