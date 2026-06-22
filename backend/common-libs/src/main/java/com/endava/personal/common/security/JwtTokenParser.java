package com.endava.personal.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenParser {

	private static final String ROLE_CLAIM = "role";
	private static final String UID_CLAIM = "uid";

	private final SecretKey secretKey;

	public JwtTokenParser(@Value("${app.security.jwt.secret}") String secret) {
		this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	public AuthPrincipal parse(String token) {
		Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();

		UUID userId = UUID.fromString(claims.get(UID_CLAIM, String.class));
		String email = claims.getSubject();
		String role = claims.get(ROLE_CLAIM, String.class);

		return new AuthPrincipal(userId, email, role);
	}
}
