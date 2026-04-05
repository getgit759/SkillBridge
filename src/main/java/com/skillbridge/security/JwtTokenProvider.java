package com.skillbridge.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Pad or trim secret to exactly 32 bytes for HS256.
     * This guarantees no key-length errors regardless of secret length.
     */
    private Key getSigningKey() {
        byte[] secretBytes = jwtSecret.getBytes();
        byte[] keyBytes = new byte[32]; // 256 bits for HS256
        System.arraycopy(
                secretBytes, 0,
                keyBytes, 0,
                Math.min(secretBytes.length, keyBytes.length)
        );
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        log.debug("Generating token for: {}", userDetails.getUsername());
        return buildToken(userDetails.getUsername());
    }

    public String generateTokenFromEmail(String email) {
        return buildToken(email);
    }

    private String buildToken(String email) {
        Date now        = new Date();
        Date expiry     = new Date(now.getTime() + jwtExpiration);

        String token = Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // ← HS256
                .compact();

        log.debug("Token generated successfully for: {}", email);
        return token;
    }

    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Invalid JWT token: {}", ex.getMessage());
            return false;
        }
    }
}