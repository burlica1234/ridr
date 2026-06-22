package com.endava.personal.auth.api.dto;

public record LoginResponseDto(String accessToken, String refreshToken, String tokenType, long expiresIn) {
}
