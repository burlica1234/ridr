package com.endava.personal.vehicle.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class UserVehicle {

    private UUID id;
    private UUID userId;
    private UUID vehicleTypeId;
    private String nickname;
    private RoutingPreference routingPreference;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
