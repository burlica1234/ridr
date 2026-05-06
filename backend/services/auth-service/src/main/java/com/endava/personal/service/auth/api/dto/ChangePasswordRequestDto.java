package com.endava.personal.service.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequestDto(

        @NotBlank
        String currentPassword,

        @NotBlank
        @Size(min = 8)
        String newPassword
) {}
