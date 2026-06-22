package com.endava.personal.rider.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record RiderProfileRequestDto(@NotBlank @Size(max = 150) String fullName,

		@Size(max = 30) String phoneNumber,

		LocalDate dateOfBirth,

		@Size(max = 100) String preferredCity,

		@Size(max = 512) String avatarUrl) {
}
