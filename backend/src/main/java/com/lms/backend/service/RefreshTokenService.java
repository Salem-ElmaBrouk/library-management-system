package com.lms.backend.service;

import com.lms.backend.entity.AppUser;
import com.lms.backend.entity.RefreshToken;
import com.lms.backend.exception.DomainExceptions;
import com.lms.backend.repository.AppUserRepository;
import com.lms.backend.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;
    private final AppUserRepository userRepository;

    // ─────────────────────────────────────────────
    // إنشاء توكن عند تسجيل الدخول
    // ─────────────────────────────────────────────
    @Transactional
    public IssuedRefreshToken createRefreshToken(String email) {
        AppUser user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new DomainExceptions.BusinessRuleException("User not found"));

        // جلسة واحدة لكل مستخدم في هذا التصميم — احذف القديمة
        refreshTokenRepository.deleteByUser(user);

        String rawToken = generateSecureRandomToken();

        RefreshToken entity = RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(rawToken))
                .expiryDate(Instant.now().plus(REFRESH_TOKEN_TTL))
                .revoked(false)
                .build();

        refreshTokenRepository.save(entity);

        // نُعيد raw token مرة واحدة فقط — الـ caller مسؤول عن تسليمه للعميل
        return new IssuedRefreshToken(rawToken, entity.getExpiryDate());
    }

    // ─────────────────────────────────────────────
    // Rotation — قلب التصميم الأمني
    // ─────────────────────────────────────────────
    @Transactional
    public RotatedTokens rotate(String rawToken) {

        String hash = hashToken(rawToken);

        // 1. اقفل الصف لمنع race condition على نفس التوكن
        RefreshToken current = refreshTokenRepository.findByTokenHashForUpdate(hash)
                .orElseThrow(() -> new BusinessRuleException("Invalid refresh token"));

        // 2. انتهت الصلاحية؟
        if (current.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(current);
            throw new DomainExceptions.BusinessRuleException("Refresh token expired");
        }

        // 3. أُعيد استخدام توكن مُدوَّر؟ → هجوم محتمل → ألغِ كل الجلسات
        if (current.isRevoked()) {
            refreshTokenRepository.deleteByUser(current.getUser());
            throw new BusinessRuleException("Token reuse detected. All sessions revoked.");
        }

        // 4. أنشئ توكن جديد قبل حذف القديم (إذا فشل الحفظ، القديم يبقى صالحاً)
        AppUser user = current.getUser();
        String newRaw = generateSecureRandomToken();

        RefreshToken next = RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(newRaw))
                .expiryDate(Instant.now().plus(REFRESH_TOKEN_TTL))
                .revoked(false)
                .build();
        refreshTokenRepository.save(next);

        // 5. احذف القديم — داخل نفس transaction
        refreshTokenRepository.delete(current);

        return new RotatedTokens(newRaw, user);
    }

    // ─────────────────────────────────────────────
    // Logout — إبطال الجلسة
    // ─────────────────────────────────────────────
    @Transactional
    public void revokeByRawToken(String rawToken) {
        refreshTokenRepository.findByTokenHash(hashToken(rawToken))
                .ifPresent(refreshTokenRepository::delete);
    }

    @Transactional
    public void revokeAllForUser(AppUser user) {
        refreshTokenRepository.deleteByUser(user);
    }

    // ─────────────────────────────────────────────
    // أدوات داخلية
    // ─────────────────────────────────────────────
    private String generateSecureRandomToken() {
        byte[] bytes = new byte[32];   // 256 bits
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String raw) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    // ─────────────────────────────────────────────
    // DTOs داخلية — عقد بين Service والـ Controller
    // ─────────────────────────────────────────────
    public record IssuedRefreshToken(String rawToken, Instant expiresAt) {}
    public record RotatedTokens(String rawToken, AppUser user) {}
}