package com.endava.personal.service.auth.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthUserRepository extends JpaRepository<AuthUserEntity, UUID> {

	Optional<AuthUserEntity> findByEmail(String email);

	boolean existsByEmail(String email);
}
