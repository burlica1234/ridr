package com.endava.personal.service.auth.service;

import com.endava.personal.service.auth.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private static final String ROLE_CLAIM = "role";

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String email, String role) {
        Instant now = Instant.now();
        Instant expiration = now.plusMillis(jwtProperties.expirationMs());

        return Jwts.builder()
                .subject(email)
                .claim(ROLE_CLAIM, role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    public String extractEmail(String token){
        return getClaimsFromToken(token).getSubject();
    }

    public String extractRole(String token){
        return getClaimsFromToken(token).get(ROLE_CLAIM, String.class);
    }

    public boolean isTokenValid(String token){
        return !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token){
        Date expiration = getClaimsFromToken(token).getExpiration();
        return  expiration.before(new Date());
    }

    private Claims getClaimsFromToken(String token){
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
