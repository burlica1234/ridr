package com.endava.personal.auth.domain;

public record AuthTokens(String accessToken, String refreshToken, String tokenType, long expiresInSeconds) {
}
