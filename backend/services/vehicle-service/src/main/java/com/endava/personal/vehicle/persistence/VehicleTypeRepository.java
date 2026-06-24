package com.endava.personal.vehicle.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VehicleTypeRepository extends JpaRepository<VehicleTypeEntity, UUID> {
}
