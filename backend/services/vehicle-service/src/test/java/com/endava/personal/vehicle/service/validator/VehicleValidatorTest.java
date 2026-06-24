package com.endava.personal.vehicle.service.validator;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.endava.personal.common.exception.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VehicleValidatorTest {

    private VehicleValidator vehicleValidator;

    @BeforeEach
    void setUp() {
        vehicleValidator = new VehicleValidator();
    }

    @Test
    void shouldPassWhenUnderLimit() {
        assertThatCode(() -> vehicleValidator.validateVehicleLimitNotReached(VehicleValidator.MAX_VEHICLES_PER_USER - 1))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectWhenLimitReached() {
        assertThatThrownBy(() -> vehicleValidator.validateVehicleLimitNotReached(VehicleValidator.MAX_VEHICLES_PER_USER))
                .isInstanceOf(ConflictException.class);
    }
}
