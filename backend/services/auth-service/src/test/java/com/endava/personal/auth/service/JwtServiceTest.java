package com.endava.personal.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.endava.personal.auth.config.JwtProperties;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-123456";
    private static final UUID USER_ID = UUID.randomUUID();
    private static final String EMAIL = "rider@ridr.io";
    private static final String ROLE = "RIDER";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(new JwtProperties(SECRET, 3_600_000L, 2_592_000_000L));
    }

    @Test
    void shouldGenerateTokenAndExtractClaims() {
        String token = jwtService.generateToken(USER_ID, EMAIL, ROLE);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractEmail(token)).isEqualTo(EMAIL);
        assertThat(jwtService.extractUserId(token)).isEqualTo(USER_ID);
        assertThat(jwtService.extractRole(token)).isEqualTo(ROLE);
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void shouldRejectTokenSignedWithDifferentSecret() {
        JwtService otherService = new JwtService(
                new JwtProperties("another-secret-another-secret-1234567890", 3_600_000L, 2_592_000_000L));
        String token = otherService.generateToken(USER_ID, EMAIL, ROLE);

        assertThatThrownBy(() -> jwtService.extractEmail(token)).isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldThrowForExpiredToken() {
        JwtService expiringService = new JwtService(new JwtProperties(SECRET, -1_000L, 2_592_000_000L));
        String token = expiringService.generateToken(USER_ID, EMAIL, ROLE);

        assertThatThrownBy(() -> jwtService.isTokenValid(token)).isInstanceOf(RuntimeException.class);
    }
}
