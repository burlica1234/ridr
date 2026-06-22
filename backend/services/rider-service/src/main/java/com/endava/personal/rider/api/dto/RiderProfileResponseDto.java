package com.endava.personal.rider.api.dto;

import java.time.LocalDate;
import java.util.UUID;

public record RiderProfileResponseDto(UUID id, UUID userId, String fullName, String phoneNumber, LocalDate dateOfBirth,
		String preferredCity, String avatarUrl) {
}
