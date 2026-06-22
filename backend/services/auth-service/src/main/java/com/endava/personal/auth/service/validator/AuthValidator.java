package com.endava.personal.auth.service.validator;

import com.endava.personal.auth.domain.AccountStatus;
import com.endava.personal.auth.domain.AuthUser;
import com.endava.personal.common.exception.ConflictException;
import com.endava.personal.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthValidator {

	private final PasswordEncoder passwordEncoder;

	private static final String AUTH_USER_REQUIRED = "Auth user is required.";
	private static final String EMAIL_REQUIRED = "Email is required.";
	private static final String PASSWORD_REQUIRED = "Password is required.";
	private static final String PASSWORD_MIN_LENGTH = "Password must contain at least 8 characters.";
	private static final String EMAIL_ALREADY_EXISTS = "Email already exists.";
	private static final String INVALID_CREDENTIALS = "Invalid credentials.";
	private static final String ACCOUNT_NOT_ACTIVE = "Account is not active.";
	private static final String ACCOUNT_LOCKED = "Account is locked.";
	private static final String CURRENT_PASSWORD_REQUIRED = "Current password is required.";
	private static final String NEW_PASSWORD_REQUIRED = "New password is required.";
	private static final String NEW_PASSWORD_MUST_BE_DIFFERENT = "New password must be different from current password.";

	public void validateRegisterBusinessRules(AuthUser authUser, String rawPassword, boolean emailExists) {
		validateAuthUser(authUser);
		validateEmail(authUser.getEmail());
		validatePassword(rawPassword);
		validateEmailDoesNotExist(emailExists);
	}

	public void validateCredentials(AuthUser authUser, String rawPassword) {
		validateAuthUserForLogin(authUser);
		validatePasswordForLogin(rawPassword);

		if (!passwordEncoder.matches(rawPassword, authUser.getPasswordHash())) {
			throw new UnauthorizedException(INVALID_CREDENTIALS);
		}

		validateAccountStatus(authUser);
	}

	public void validateAccountStatus(AuthUser authUser) {
		validateAuthUser(authUser);

		if (authUser.getAccountStatus() == AccountStatus.LOCKED) {
			throw new ConflictException(ACCOUNT_LOCKED);
		}

		if (authUser.getAccountStatus() != AccountStatus.ACTIVE) {
			throw new ConflictException(ACCOUNT_NOT_ACTIVE);
		}
	}

	public void validateChangePasswordBusinessRules(AuthUser authUser, String currentPassword, String newPassword) {
		validateAuthUser(authUser);
		validateAccountStatus(authUser);
		validateCurrentPassword(currentPassword);
		validateNewPassword(newPassword);
		validateNewPasswordIsDifferent(currentPassword, newPassword);

		if (!passwordEncoder.matches(currentPassword, authUser.getPasswordHash())) {
			throw new UnauthorizedException(INVALID_CREDENTIALS);
		}
	}

	public void validateEmailDoesNotExist(boolean emailExists) {
		if (emailExists) {
			throw new ConflictException(EMAIL_ALREADY_EXISTS);
		}
	}

	private void validateAuthUser(AuthUser authUser) {
		if (authUser == null) {
			throw new ConflictException(AUTH_USER_REQUIRED);
		}
	}

	private void validateAuthUserForLogin(AuthUser authUser) {
		if (authUser == null) {
			throw new UnauthorizedException(INVALID_CREDENTIALS);
		}
	}

	private void validateEmail(String email) {
		if (email == null || email.isBlank()) {
			throw new ConflictException(EMAIL_REQUIRED);
		}
	}

	private void validatePassword(String password) {
		if (password == null || password.isBlank()) {
			throw new ConflictException(PASSWORD_REQUIRED);
		}

		if (password.length() < 8) {
			throw new ConflictException(PASSWORD_MIN_LENGTH);
		}
	}

	private void validatePasswordForLogin(String password) {
		if (password == null || password.isBlank()) {
			throw new UnauthorizedException(INVALID_CREDENTIALS);
		}
	}

	private void validateCurrentPassword(String currentPassword) {
		if (currentPassword == null || currentPassword.isBlank()) {
			throw new ConflictException(CURRENT_PASSWORD_REQUIRED);
		}
	}

	private void validateNewPassword(String newPassword) {
		if (newPassword == null || newPassword.isBlank()) {
			throw new ConflictException(NEW_PASSWORD_REQUIRED);
		}

		if (newPassword.length() < 8) {
			throw new ConflictException(PASSWORD_MIN_LENGTH);
		}
	}

	private void validateNewPasswordIsDifferent(String currentPassword, String newPassword) {
		if (currentPassword.equals(newPassword)) {
			throw new ConflictException(NEW_PASSWORD_MUST_BE_DIFFERENT);
		}
	}

}
