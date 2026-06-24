package com.endava.personal.vehicle.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserVehicleRepository extends JpaRepository<UserVehicleEntity, UUID> {

    List<UserVehicleEntity> findByUserId(UUID userId);

    Optional<UserVehicleEntity> findByIdAndUserId(UUID id, UUID userId);

    long countByUserId(UUID userId);
}
