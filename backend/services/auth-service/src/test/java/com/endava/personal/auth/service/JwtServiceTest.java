package com.endava.personal.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.endava.personal.auth.config.JwtProperties;
import com.endava.personal.common.security.AuthPrincipal;
import com.endava.personal.common.security.JwtTokenParser;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

	private static final String SECRET = "test-secret-test-secret-test-secret-123456";
	private static final UUID USER_ID = UUID.randomUUID();
	private static final String EMAIL = "rider@ridr.io";
	private static final String ROLE = "RIDER";

	private JwtService jwtService;
	private JwtTokenParser jwtTokenParser;

	@BeforeEach
	void setUp() {
		jwtService = new JwtService(new JwtProperties(SECRET, 3_600_000L, 2_592_000_000L));
		jwtTokenParser = new JwtTokenParser(SECRET);
	}

	@Test
	void shouldGenerateTokenCarryingUserClaims() {
		String token = jwtService.generateToken(USER_ID, EMAIL, ROLE);

		AuthPrincipal principal = jwtTokenParser.parse(token);

		assertThat(principal.userId()).isEqualTo(USER_ID);
		assertThat(principal.email()).isEqualTo(EMAIL);
		assertThat(principal.role()).isEqualTo(ROLE);
	}

	@Test
	void shouldProduceTokenRejectedByParserWithDifferentSecret() {
		String token = jwtService.generateToken(USER_ID, EMAIL, ROLE);
		JwtTokenParser otherParser = new JwtTokenParser("another-secret-another-secret-1234567890");

		assertThatThrownBy(() -> otherParser.parse(token)).isInstanceOf(RuntimeException.class);
	}

	@Test
	void shouldProduceExpiredTokenRejectedByParser() {
		JwtService expiringService = new JwtService(new JwtProperties(SECRET, -1_000L, 2_592_000_000L));
		String token = expiringService.generateToken(USER_ID, EMAIL, ROLE);

		assertThatThrownBy(() -> jwtTokenParser.parse(token)).isInstanceOf(RuntimeException.class);
	}
}
