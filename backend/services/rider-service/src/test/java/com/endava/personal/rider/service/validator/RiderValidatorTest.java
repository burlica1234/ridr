package com.endava.personal.rider.service.validator;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.endava.personal.common.exception.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RiderValidatorTest {

	private RiderValidator riderValidator;

	@BeforeEach
	void setUp() {
		riderValidator = new RiderValidator();
	}

	@Test
	void shouldPassWhenProfileDoesNotExist() {
		assertThatCode(() -> riderValidator.validateProfileDoesNotExist(false)).doesNotThrowAnyException();
	}

	@Test
	void shouldRejectWhenProfileAlreadyExists() {
		assertThatThrownBy(() -> riderValidator.validateProfileDoesNotExist(true))
				.isInstanceOf(ConflictException.class);
	}
}
