package com.example.basicweb.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    // 256-bit secret key for HMAC-SHA256
    private static final String SECRET_STRING = "basic_web_super_secret_jwt_key_that_is_at_least_256_bits_long_12345";
    
    // 24 hours in milliseconds: 24 * 60 * 60 * 1000
    public static final long EXPIRATION_TIME_MS = 24L * 60 * 60 * 1000;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate a JWT token valid for 24 hours with email as subject and username claim.
     */
    public String generateToken(String email, String username) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(email)
                .claim("username", username)
                .claim("appName", "basic-web")
                .issuedAt(new Date(now))
                .expiration(new Date(now + EXPIRATION_TIME_MS))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateToken(String email) {
        return generateToken(email, email);
    }

    /**
     * Extract email (subject) from JWT token.
     */
    public String extractEmail(String token) {
        Claims claims = extractAllClaims(token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * Extract username claim from JWT token.
     */
    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        if (claims == null) {
            return null;
        }
        String username = claims.get("username", String.class);
        return username != null ? username : claims.getSubject();
    }

    /**
     * Validate JWT token format, signature, and expiration.
     */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            if (claims == null) {
                return false;
            }
            return !isTokenExpired(claims);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration != null && expiration.before(new Date());
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
