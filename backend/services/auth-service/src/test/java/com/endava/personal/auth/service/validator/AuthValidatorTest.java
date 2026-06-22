package com.endava.personal.auth.service.validator;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.endava.personal.auth.domain.AccountStatus;
import com.endava.personal.auth.domain.AuthUser;
import com.endava.personal.auth.domain.Role;
import com.endava.personal.common.exception.ConflictException;
import com.endava.personal.common.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthValidatorTest {

	private static final String EMAIL = "rider@ridr.io";
	private static final String RAW_PASSWORD = "supersecret";
	private static final String PASSWORD_HASH = "hashed-supersecret";

	@Mock
	private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

	private AuthValidator authValidator;

	@BeforeEach
	void setUp() {
		authValidator = new AuthValidator(passwordEncoder);
	}

	private AuthUser activeUser() {
		AuthUser user = new AuthUser();
		user.setEmail(EMAIL);
		user.setPasswordHash(PASSWORD_HASH);
		user.setRole(Role.RIDER);
		user.setAccountStatus(AccountStatus.ACTIVE);
		return user;
	}

	@Test
	void shouldPassRegisterValidationForValidInput() {
		assertThatCode(() -> authValidator.validateRegisterBusinessRules(activeUser(), RAW_PASSWORD, false))
				.doesNotThrowAnyException();
	}

	@Test
	void shouldRejectRegisterWhenEmailExists() {
		assertThatThrownBy(() -> authValidator.validateRegisterBusinessRules(activeUser(), RAW_PASSWORD, true))
				.isInstanceOf(ConflictException.class);
	}

	@Test
	void shouldRejectRegisterWhenPasswordTooShort() {
		assertThatThrownBy(() -> authValidator.validateRegisterBusinessRules(activeUser(), "short", false))
				.isInstanceOf(ConflictException.class);
	}

	@Test
	void shouldRejectCredentialsWhenPasswordDoesNotMatch() {
		when(passwordEncoder.matches(RAW_PASSWORD, PASSWORD_HASH)).thenReturn(false);

		assertThatThrownBy(() -> authValidator.validateCredentials(activeUser(), RAW_PASSWORD))
				.isInstanceOf(UnauthorizedException.class);
	}

	@Test
	void shouldRejectLockedAccountStatus() {
		AuthUser user = activeUser();
		user.setAccountStatus(AccountStatus.LOCKED);

		assertThatThrownBy(() -> authValidator.validateAccountStatus(user)).isInstanceOf(ConflictException.class);
	}

	@Test
	void shouldRejectInactiveAccountStatus() {
		AuthUser user = activeUser();
		user.setAccountStatus(AccountStatus.INACTIVE);

		assertThatThrownBy(() -> authValidator.validateAccountStatus(user)).isInstanceOf(ConflictException.class);
	}

	@Test
	void shouldRejectChangePasswordWhenNewEqualsCurrent() {
		assertThatThrownBy(
				() -> authValidator.validateChangePasswordBusinessRules(activeUser(), RAW_PASSWORD, RAW_PASSWORD))
				.isInstanceOf(ConflictException.class);
	}

	@Test
	void shouldRejectChangePasswordWhenCurrentPasswordWrong() {
		when(passwordEncoder.matches("currentpass", PASSWORD_HASH)).thenReturn(false);

		assertThatThrownBy(
				() -> authValidator.validateChangePasswordBusinessRules(activeUser(), "currentpass", "newpassword"))
				.isInstanceOf(UnauthorizedException.class);
	}
}
