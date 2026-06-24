package com.endava.personal.vehicle.api.dto;

import java.util.UUID;

public record VehicleTypeResponseDto(UUID id, String code, String displayName, Integer defaultMaxSpeed) {
}
