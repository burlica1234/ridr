package com.endava.personal.rider.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiderProfileRepository extends JpaRepository<RiderProfileEntity, UUID> {

	Optional<RiderProfileEntity> findByUserId(UUID id);

	boolean existsByUserId(UUID id);
}
