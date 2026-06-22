package com.endava.personal.rider.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "rider_profiles")
@Getter
@Setter
@NoArgsConstructor
public class RiderProfileEntity {

	@Id
	private UUID id;

	@Column(nullable = false, unique = true)
	private UUID userId;

	@Column(nullable = false, length = 150)
	private String fullName;

	@Column(length = 30)
	private String phoneNumber;

	@Column
	private LocalDate dateOfBirth;

	@Column(length = 100)
	private String preferredCity;

	@Column(length = 512)
	private String avatarUrl;

	@Column(nullable = false)
	private OffsetDateTime createdAt;

	@Column(nullable = false)
	private OffsetDateTime updatedAt;
}
