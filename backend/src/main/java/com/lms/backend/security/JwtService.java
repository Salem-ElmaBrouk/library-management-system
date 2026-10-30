
package com.lms.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    // No insecure fallback secret
    @Value("${JWT_SECRET}")
    private String secretKey;

    private static final long ACCESS_TOKEN_EXPIRATION =
            1000L * 60 * 15; // 15 minutes

    private static final long REFRESH_TOKEN_EXPIRATION =
            1000L * 60 * 60 * 24 * 7; // 7 days

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver) {

        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateAccessToken(String email) {
        return generateToken(email, "access", ACCESS_TOKEN_EXPIRATION);
    }

    public String generateRefreshToken(String email) {
        return generateToken(email, "refresh", REFRESH_TOKEN_EXPIRATION);
    }

    private String generateToken(
            String email,
            String tokenType,
            long expiration) {

        long now = System.currentTimeMillis();

        return Jwts.builder()
                .subject(email)
                .claim("type", tokenType)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expiration))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    // Access tokens must not accept refresh tokens
    public boolean isAccessTokenValid(String token, String userEmail) {
        return isTokenValid(token, userEmail, "access");
    }

    // Refresh tokens must not accept access tokens
    public boolean isRefreshTokenValid(String token, String userEmail) {
        return isTokenValid(token, userEmail, "refresh");
    }

    private boolean isTokenValid(
            String token,
            String userEmail,
            String expectedType) {

        Claims claims = extractAllClaims(token);

        String username = claims.getSubject();
        String tokenType = claims.get("type", String.class);

        return username != null
                && username.equalsIgnoreCase(userEmail)
                && expectedType.equals(tokenType)
                && claims.getExpiration() != null
                && claims.getExpiration().after(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET must contain at least 32 bytes."
            );
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }
}
