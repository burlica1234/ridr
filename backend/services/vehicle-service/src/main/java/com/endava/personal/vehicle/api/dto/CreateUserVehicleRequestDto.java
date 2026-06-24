package com.endava.personal.vehicle.api.dto;

import com.endava.personal.vehicle.domain.RoutingPreference;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateUserVehicleRequestDto(@NotNull UUID vehicleTypeId,

                                          @Size(max = 80) String nickname,

                                          @NotNull RoutingPreference routingPreference) {
}
