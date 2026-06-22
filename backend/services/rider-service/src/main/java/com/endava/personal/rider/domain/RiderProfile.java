package com.endava.personal.rider.domain;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RiderProfile {

	private UUID id;
	private UUID userId;
	private String fullName;
	private String phoneNumber;
	private LocalDate dateOfBirth;
	private String preferredCity;
	private String avatarUrl;
	private OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;
}
