package com.endava.personal.common.exception;

import org.springframework.http.HttpStatus;

public class FieldNotUpdatableException extends BusinessException {
	public FieldNotUpdatableException(String message) {
		super(HttpStatus.CONFLICT, "FIELD_NOT_UPDATABLE", message);
	}
}
