package com.endava.personal.service.auth.api.dto;

public record LoginResponseDto(
        String accessToken,
        String tokenType
) {}
