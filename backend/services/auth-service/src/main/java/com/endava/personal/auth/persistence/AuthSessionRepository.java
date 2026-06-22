package com.endava.personal.auth.persistence;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthSessionRepository extends JpaRepository<AuthSessionEntity, UUID> {

    Optional<AuthSessionEntity> findByRefreshTokenHash(String refreshTokenHash);

    @Modifying
    @Query("update AuthSessionEntity s set s.revokedAt = :revokedAt "
            + "where s.userId = :userId and s.revokedAt is null")
    int revokeAllActiveForUser(@Param("userId") UUID userId, @Param("revokedAt") OffsetDateTime revokedAt);
}
