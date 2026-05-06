package com.endava.personal.service.auth.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuthUserRepository extends JpaRepository<AuthUserEntity, UUID>{

    Optional<AuthUserEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
