package com.endava.personal.vehicle.persistence;

import com.endava.personal.vehicle.domain.RoutingPreference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_vehicles")
@Getter
@Setter
@NoArgsConstructor
public class UserVehicleEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID vehicleTypeId;

    @Column(length = 80)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RoutingPreference routingPreference;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;
}
