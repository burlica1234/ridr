package com.endava.personal.vehicle.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class VehicleType {

    private UUID id;
    private String code;
    private String displayName;
    private Integer defaultMaxSpeed;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
