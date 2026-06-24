package com.endava.personal.vehicle.service.validator;

import com.endava.personal.common.exception.ConflictException;
import org.springframework.stereotype.Component;

@Component
public class VehicleValidator {

    static final int MAX_VEHICLES_PER_USER = 10;

    private static final String LIMIT_REACHED = "Maximum number of vehicles per user reached.";

    public void validateVehicleLimitNotReached(long currentVehicleCount) {
        if (currentVehicleCount >= MAX_VEHICLES_PER_USER) {
            throw new ConflictException(LIMIT_REACHED);
        }
    }
}
