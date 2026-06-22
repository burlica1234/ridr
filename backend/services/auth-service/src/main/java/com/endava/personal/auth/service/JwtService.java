package com.endava.personal.auth.service;

import com.endava.personal.auth.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

	private static final String ROLE_CLAIM = "role";
	private static final String UID_CLAIM = "uid";

	private final JwtProperties jwtProperties;
	private final SecretKey secretKey;

	public JwtService(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
		this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
	}

	public String generateToken(UUID userId, String email, String role) {
		Instant now = Instant.now();
		Instant expiration = now.plusMillis(jwtProperties.expirationMs());

		return Jwts.builder().subject(email).claim(UID_CLAIM, userId.toString()).claim(ROLE_CLAIM, role)
				.issuedAt(Date.from(now)).expiration(Date.from(expiration)).signWith(secretKey).compact();
	}
}
