package com.endava.personal.service.auth.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(
        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(min = 8, message = "Password must be at least 8 characters long", max=100)
        String password
) {}
