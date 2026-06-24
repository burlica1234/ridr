package com.endava.personal.vehicle.service;

import com.endava.personal.common.exception.NotFoundException;
import com.endava.personal.vehicle.domain.UserVehicle;
import com.endava.personal.vehicle.domain.VehicleType;
import com.endava.personal.vehicle.mapper.VehicleMapper;
import com.endava.personal.vehicle.persistence.UserVehicleEntity;
import com.endava.personal.vehicle.persistence.UserVehicleRepository;
import com.endava.personal.vehicle.persistence.VehicleTypeRepository;
import com.endava.personal.vehicle.service.validator.VehicleValidator;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleTypeRepository vehicleTypeRepository;
    private final UserVehicleRepository userVehicleRepository;
    private final VehicleValidator vehicleValidator;
    private final VehicleMapper vehicleMapper;

    private static final String VEHICLE_TYPE_WITH_ID = "Vehicle type with id ";
    private static final String VEHICLE_WITH_ID = "Vehicle with id ";
    private static final String NOT_FOUND = " not found.";

    @Transactional(readOnly = true)
    public List<VehicleType> getVehicleTypes() {
        return vehicleTypeRepository.findAll().stream()
                .map(vehicleMapper::entityToDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserVehicle> getVehiclesForUser(UUID userId) {
        return userVehicleRepository.findByUserId(userId).stream()
                .map(vehicleMapper::entityToDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserVehicle addVehicle(UserVehicle userVehicle) {
        ensureVehicleTypeExists(userVehicle.getVehicleTypeId());
        vehicleValidator.validateVehicleLimitNotReached(userVehicleRepository.countByUserId(userVehicle.getUserId()));

        OffsetDateTime now = OffsetDateTime.now();
        userVehicle.setId(UUID.randomUUID());
        userVehicle.setCreatedAt(now);
        userVehicle.setUpdatedAt(now);

        UserVehicleEntity savedEntity = userVehicleRepository.save(vehicleMapper.domainToEntity(userVehicle));

        return  vehicleMapper.entityToDomain(savedEntity);
    }

    @Transactional
    public void deleteVehicle(UUID userId, UUID vehicleId) {
        UserVehicleEntity entity = userVehicleRepository.findByIdAndUserId(vehicleId, userId)
                .orElseThrow(() -> new NotFoundException(VEHICLE_WITH_ID + vehicleId + NOT_FOUND));
        userVehicleRepository.delete(entity);
    }

    private void ensureVehicleTypeExists(UUID vehicleTypeId) {
        if (!vehicleTypeRepository.existsById(vehicleTypeId)) {
            throw new NotFoundException(VEHICLE_TYPE_WITH_ID + vehicleTypeId + NOT_FOUND);
        }
    }
}
