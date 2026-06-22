package com.endava.personal.auth.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RefreshTokenServiceTest {

    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService();
    }

    @Test
    void shouldGenerateUniqueTokens() {
        String first = refreshTokenService.generateRawToken();
        String second = refreshTokenService.generateRawToken();

        assertThat(first).isNotBlank();
        assertThat(second).isNotBlank();
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void shouldHashDeterministicallyAndNotExposeRawToken() {
        String raw = refreshTokenService.generateRawToken();

        String hashOne = refreshTokenService.hash(raw);
        String hashTwo = refreshTokenService.hash(raw);

        assertThat(hashOne).isEqualTo(hashTwo);
        assertThat(hashOne).isNotEqualTo(raw);
    }

    @Test
    void shouldProduceDifferentHashesForDifferentTokens() {
        assertThat(refreshTokenService.hash("token-a")).isNotEqualTo(refreshTokenService.hash("token-b"));
    }
}
