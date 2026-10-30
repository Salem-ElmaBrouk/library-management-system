package com.lms.backend.controller;

import com.lms.backend.entity.AppUser;
import com.lms.backend.entity.RefreshToken;
import com.lms.backend.exception.DomainExceptions;
import com.lms.backend.security.JwtService;
import com.lms.backend.service.AuthService;
import com.lms.backend.service.RefreshTokenService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    // DTOs
    public record RegisterRequest(@Email String email, @NotBlank String password, String firstName, String lastName) {}
    public record LoginRequest(@Email String email, @NotBlank String password) {}
    public record AuthResponse(String accessToken, String refreshToken, String role) {}
    public record TokenRefreshRequest(@NotBlank String refreshToken) {}
    public record ForgotPasswordRequest(@Email String email) {}
    public record ResetPasswordRequest(@NotBlank String token, @NotBlank String password) {}

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request.email(), request.password(), request.firstName(), request.lastName());
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        AppUser user = authService.findByEmail(request.email());
        String accessToken = jwtService.generateAccessToken(user.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getEmail());

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken.getToken(), user.getRole().name()));
    }

    @PostMapping("/refresh")
    @Transactional
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        // استخدم قفل pessimistic لمنع race conditions
        RefreshToken oldToken = refreshTokenRepository.findByTokenForUpdate(request.refreshToken())
                .orElseThrow(() -> new DomainExceptions.BusinessRuleException("Invalid refresh token"));

        if (oldToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(oldToken);
            throw new DomainExceptions.BusinessRuleException("Refresh token expired");
        }

        if (oldToken.isRevoked()) {
            // REUSE DETECTED — ألغِ كل عائلة التوكنات
            refreshTokenRepository.deleteAllByUser(oldToken.getUser());
            throw new DomainExceptions.BusinessRuleException("Token reuse detected. All sessions revoked.");
        }

        // ادمج: أنشئ توكن جديد واحذف القديم في نفس الترانزاكشن
        AppUser user = oldToken.getUser();
        refreshTokenRepository.delete(oldToken);

        String newRefresh = generateSecureRandomToken();
        RefreshToken newToken = RefreshToken.builder()
                .user(user)
                .tokenHash(hash(newRefresh))
                .expiryDate(Instant.now().plus(7, ChronoUnit.DAYS))
                .revoked(false)
                .build();
        refreshTokenRepository.save(newToken);

        String newAccess = jwtService.generateAccessToken(user.getEmail());
        return ResponseEntity.ok(new AuthResponse(newAccess, newRefresh, user.getRole().name()));
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.createPasswordResetToken(request.email());
        return ResponseEntity.ok("If an account with that email exists, a reset link has been sent.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.token(), request.password());
        return ResponseEntity.ok("Password successfully reset. You can now log in.");
    }
}