package com.endava.personal.vehicle.mapper;

import com.endava.personal.vehicle.api.dto.CreateUserVehicleRequestDto;
import com.endava.personal.vehicle.api.dto.UserVehicleResponseDto;
import com.endava.personal.vehicle.api.dto.VehicleTypeResponseDto;
import com.endava.personal.vehicle.domain.UserVehicle;
import com.endava.personal.vehicle.domain.VehicleType;
import com.endava.personal.vehicle.persistence.UserVehicleEntity;
import com.endava.personal.vehicle.persistence.VehicleTypeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VehicleMapper {

    VehicleType entityToDomain(VehicleTypeEntity entity);

    VehicleTypeResponseDto domainToResponse(VehicleType vehicleType);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserVehicle requestToDomain(CreateUserVehicleRequestDto request);

    UserVehicleEntity domainToEntity(UserVehicle userVehicle);

    UserVehicle entityToDomain(UserVehicleEntity entity);

    UserVehicleResponseDto domainToResponse(UserVehicle userVehicle);
}
