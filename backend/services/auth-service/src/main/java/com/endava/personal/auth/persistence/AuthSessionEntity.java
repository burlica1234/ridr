package com.endava.personal.auth.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "auth_sessions")
@Getter
@Setter
@NoArgsConstructor
public class AuthSessionEntity {

	@Id
	private UUID id;

	@Column(nullable = false)
	private UUID userId;

	@Column(nullable = false, unique = true, length = 255)
	private String refreshTokenHash;

	@Column(nullable = false)
	private OffsetDateTime expiresAt;

	@Column
	private OffsetDateTime revokedAt;

	@Column(nullable = false)
	private OffsetDateTime createdAt;
}
