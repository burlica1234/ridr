package com.endava.personal.vehicle.api.dto;

import com.endava.personal.vehicle.domain.RoutingPreference;

import java.util.UUID;

public record UserVehicleResponseDto(UUID id, UUID userId, UUID vehicleTypeId, String nickname,
                                     RoutingPreference routingPreference) {
}
