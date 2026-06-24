package com.endava.personal.vehicle.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.endava.personal.common.exception.NotFoundException;
import com.endava.personal.vehicle.domain.RoutingPreference;
import com.endava.personal.vehicle.domain.UserVehicle;
import com.endava.personal.vehicle.mapper.VehicleMapper;
import com.endava.personal.vehicle.persistence.UserVehicleEntity;
import com.endava.personal.vehicle.persistence.UserVehicleRepository;
import com.endava.personal.vehicle.persistence.VehicleTypeRepository;
import com.endava.personal.vehicle.service.validator.VehicleValidator;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleTypeRepository vehicleTypeRepository;
    @Mock
    private UserVehicleRepository userVehicleRepository;
    @Mock
    private VehicleValidator vehicleValidator;
    @Mock
    private VehicleMapper vehicleMapper;

    private VehicleService vehicleService;

    @BeforeEach
    void setUp() {
        vehicleService = new VehicleService(vehicleTypeRepository, userVehicleRepository, vehicleValidator,
                vehicleMapper);
    }

    private UserVehicle vehicle(UUID userId, UUID typeId) {
        UserVehicle vehicle = new UserVehicle();
        vehicle.setUserId(userId);
        vehicle.setVehicleTypeId(typeId);
        vehicle.setRoutingPreference(RoutingPreference.SAFEST);
        return vehicle;
    }

    @Test
    void shouldAddVehicleWithGeneratedIdAndTimestamps() {
        UUID userId = UUID.randomUUID();
        UUID typeId = UUID.randomUUID();
        UserVehicle input = vehicle(userId, typeId);
        UserVehicleEntity entity = new UserVehicleEntity();
        UserVehicleEntity savedEntity = new UserVehicleEntity();
        UserVehicle mapped = vehicle(userId, typeId);

        when(vehicleTypeRepository.existsById(typeId)).thenReturn(true);
        when(userVehicleRepository.countByUserId(userId)).thenReturn(2L);
        when(vehicleMapper.domainToEntity(input)).thenReturn(entity);
        when(userVehicleRepository.save(entity)).thenReturn(savedEntity);
        when(vehicleMapper.entityToDomain(savedEntity)).thenReturn(mapped);

        UserVehicle result = vehicleService.addVehicle(input);

        assertThat(result).isSameAs(mapped);
        assertThat(input.getId()).isNotNull();
        assertThat(input.getCreatedAt()).isNotNull();
        assertThat(input.getUpdatedAt()).isNotNull();
        verify(vehicleValidator).validateVehicleLimitNotReached(2L);
    }

    @Test
    void shouldRejectAddWhenVehicleTypeMissing() {
        UUID userId = UUID.randomUUID();
        UUID typeId = UUID.randomUUID();

        when(vehicleTypeRepository.existsById(typeId)).thenReturn(false);

        assertThatThrownBy(() -> vehicleService.addVehicle(vehicle(userId, typeId)))
                .isInstanceOf(NotFoundException.class);
        verify(userVehicleRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldDeleteOwnVehicle() {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        UserVehicleEntity entity = new UserVehicleEntity();

        when(userVehicleRepository.findByIdAndUserId(vehicleId, userId)).thenReturn(Optional.of(entity));

        vehicleService.deleteVehicle(userId, vehicleId);

        verify(userVehicleRepository).delete(entity);
    }

    @Test
    void shouldRejectDeleteWhenVehicleNotOwnedOrMissing() {
        UUID userId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();

        when(userVehicleRepository.findByIdAndUserId(vehicleId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.deleteVehicle(userId, vehicleId)).isInstanceOf(NotFoundException.class);
    }
}
