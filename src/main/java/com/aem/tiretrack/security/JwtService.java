package com.aem.tiretrack.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.WeakKeyException;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationTime;

    @Value("${jwt.issuer:TireTrack}")
    private String issuer;

    @Value("${jwt.audience:TireTrack-Web}")
    private String audience;

    private SecretKey getSigningKey() {
        String configuredSecret = this.secretKey == null ? "" : this.secretKey.trim();
        try {
            byte[] decodedKey = Decoders.BASE64.decode(configuredSecret);
            if (decodedKey.length >= 32) {
                return Keys.hmacShaKeyFor(decodedKey);
            }
        } catch (RuntimeException ex) {
            // Plain-text secrets are supported for local/demo setup.
        }

        byte[] keyBytes = configuredSecret.getBytes(StandardCharsets.UTF_8);
        try {
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (WeakKeyException ex) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 characters long or a Base64-encoded 256-bit key.", ex);
        }
    }

    public String generateToken(String email) {
        long now = System.currentTimeMillis();

        // TODO SaaS Phase 2: add shop claims after tenant assignment becomes mandatory.
        return Jwts.builder()
                .setSubject(email)
                .setIssuer(issuer)
                .setAudience(audience)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationTime))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .requireIssuer(issuer)
                .requireAudience(audience)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public boolean isTokenValid(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .requireIssuer(issuer)
                .requireAudience(audience)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getExpiration().after(new Date());
    }
}
