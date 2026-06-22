package com.endava.personal.rider.service.validator;

import com.endava.personal.common.exception.ConflictException;
import org.springframework.stereotype.Component;

@Component
public class RiderValidator {

	private static final String PROFILE_ALREADY_EXISTS = "Rider profile already exists for this user.";

	public void validateProfileDoesNotExist(boolean profileExists) {
		if (profileExists) {
			throw new ConflictException(PROFILE_ALREADY_EXISTS);
		}
	}
}
