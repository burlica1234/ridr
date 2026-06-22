package com.endava.personal.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.endava.personal.auth.config.JwtProperties;
import com.endava.personal.auth.domain.AccountStatus;
import com.endava.personal.auth.domain.AuthTokens;
import com.endava.personal.auth.domain.AuthUser;
import com.endava.personal.auth.domain.Role;
import com.endava.personal.auth.mapper.AuthMapper;
import com.endava.personal.auth.persistence.AuthSessionEntity;
import com.endava.personal.auth.persistence.AuthSessionRepository;
import com.endava.personal.auth.persistence.AuthUserEntity;
import com.endava.personal.auth.persistence.AuthUserRepository;
import com.endava.personal.auth.service.validator.AuthValidator;
import com.endava.personal.common.exception.UnauthorizedException;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	private static final String EMAIL = "rider@ridr.io";
	private static final String RAW_PASSWORD = "supersecret";
	private static final String ACCESS_TOKEN = "access-token";
	private static final String RAW_REFRESH = "raw-refresh-token";
	private static final String REFRESH_HASH = "refresh-hash";
	private static final long ACCESS_EXPIRATION_MS = 3_600_000L;

	@Mock
	private AuthUserRepository authUserRepository;
	@Mock
	private AuthSessionRepository authSessionRepository;
	@Mock
	private AuthValidator authValidator;
	@Mock
	private AuthMapper authMapper;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private JwtService jwtService;
	@Mock
	private RefreshTokenService refreshTokenService;

	private AuthService authService;

	@BeforeEach
	void setUp() {
		JwtProperties jwtProperties = new JwtProperties("secret-secret-secret-secret-123456", ACCESS_EXPIRATION_MS,
				2_592_000_000L);
		authService = new AuthService(authUserRepository, authSessionRepository, authValidator, authMapper,
				passwordEncoder, jwtService, refreshTokenService, jwtProperties);
	}

	private AuthUser activeUser(UUID id) {
		AuthUser user = new AuthUser();
		user.setId(id);
		user.setEmail(EMAIL);
		user.setRole(Role.RIDER);
		user.setAccountStatus(AccountStatus.ACTIVE);
		return user;
	}

	private void stubTokenIssuing() {
        when(jwtService.generateToken(any(), eq(EMAIL), eq(Role.RIDER.name()))).thenReturn(ACCESS_TOKEN);
		when(refreshTokenService.generateRawToken()).thenReturn(RAW_REFRESH);
		when(refreshTokenService.hash(RAW_REFRESH)).thenReturn(REFRESH_HASH);
	}

	@Test
	void shouldLoginAndIssueTokensWithPersistedSession() {
		UUID userId = UUID.randomUUID();
		AuthUserEntity entity = new AuthUserEntity();
		AuthUser user = activeUser(userId);

		when(authUserRepository.findByEmail(EMAIL)).thenReturn(Optional.of(entity));
		when(authMapper.entityToDomain(entity)).thenReturn(user);
		stubTokenIssuing();

		AuthTokens tokens = authService.login(EMAIL, RAW_PASSWORD);

		assertThat(tokens.accessToken()).isEqualTo(ACCESS_TOKEN);
		assertThat(tokens.refreshToken()).isEqualTo(RAW_REFRESH);
		assertThat(tokens.tokenType()).isEqualTo("Bearer");
		assertThat(tokens.expiresInSeconds()).isEqualTo(ACCESS_EXPIRATION_MS / 1000);

		verify(authValidator).validateCredentials(user, RAW_PASSWORD);

		ArgumentCaptor<AuthSessionEntity> sessionCaptor = ArgumentCaptor.forClass(AuthSessionEntity.class);
		verify(authSessionRepository).save(sessionCaptor.capture());
		AuthSessionEntity savedSession = sessionCaptor.getValue();
		assertThat(savedSession.getUserId()).isEqualTo(userId);
		assertThat(savedSession.getRefreshTokenHash()).isEqualTo(REFRESH_HASH);
		assertThat(savedSession.getRevokedAt()).isNull();
		assertThat(savedSession.getExpiresAt()).isAfter(OffsetDateTime.now());
	}

	@Test
	void shouldRejectLoginWhenUserNotFound() {
		when(authUserRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> authService.login(EMAIL, RAW_PASSWORD)).isInstanceOf(UnauthorizedException.class);
		verify(authSessionRepository, never()).save(any());
	}

	@Test
	void shouldRefreshAndRotateSession() {
		UUID userId = UUID.randomUUID();
		AuthSessionEntity session = new AuthSessionEntity();
		session.setId(UUID.randomUUID());
		session.setUserId(userId);
		session.setRefreshTokenHash(REFRESH_HASH);
		session.setExpiresAt(OffsetDateTime.now().plusDays(1));
		session.setCreatedAt(OffsetDateTime.now());

		AuthUserEntity entity = new AuthUserEntity();
		AuthUser user = activeUser(userId);

		when(refreshTokenService.hash(RAW_REFRESH)).thenReturn(REFRESH_HASH);
		when(authSessionRepository.findByRefreshTokenHash(REFRESH_HASH)).thenReturn(Optional.of(session));
		when(authUserRepository.findById(userId)).thenReturn(Optional.of(entity));
		when(authMapper.entityToDomain(entity)).thenReturn(user);
        when(jwtService.generateToken(any(), eq(EMAIL), eq(Role.RIDER.name()))).thenReturn(ACCESS_TOKEN);
		when(refreshTokenService.generateRawToken()).thenReturn("rotated-refresh");
		when(refreshTokenService.hash("rotated-refresh")).thenReturn("rotated-hash");

		AuthTokens tokens = authService.refresh(RAW_REFRESH);

		assertThat(tokens.refreshToken()).isEqualTo("rotated-refresh");
		assertThat(session.getRevokedAt()).isNotNull();
		verify(authValidator).validateAccountStatus(user);

		ArgumentCaptor<AuthSessionEntity> sessionCaptor = ArgumentCaptor.forClass(AuthSessionEntity.class);
		verify(authSessionRepository, times(2)).save(sessionCaptor.capture());
		assertThat(sessionCaptor.getAllValues()).anyMatch(s -> "rotated-hash".equals(s.getRefreshTokenHash()));
	}

	@Test
	void shouldRejectRefreshWhenTokenUnknown() {
		when(refreshTokenService.hash(RAW_REFRESH)).thenReturn(REFRESH_HASH);
		when(authSessionRepository.findByRefreshTokenHash(REFRESH_HASH)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> authService.refresh(RAW_REFRESH)).isInstanceOf(UnauthorizedException.class);
	}

	@Test
	void shouldRejectRefreshWhenSessionExpired() {
		AuthSessionEntity session = new AuthSessionEntity();
		session.setExpiresAt(OffsetDateTime.now().minusMinutes(1));

		when(refreshTokenService.hash(RAW_REFRESH)).thenReturn(REFRESH_HASH);
		when(authSessionRepository.findByRefreshTokenHash(REFRESH_HASH)).thenReturn(Optional.of(session));

		assertThatThrownBy(() -> authService.refresh(RAW_REFRESH)).isInstanceOf(UnauthorizedException.class);
	}

	@Test
	void shouldRejectRefreshWhenSessionRevoked() {
		AuthSessionEntity session = new AuthSessionEntity();
		session.setExpiresAt(OffsetDateTime.now().plusDays(1));
		session.setRevokedAt(OffsetDateTime.now().minusMinutes(1));

		when(refreshTokenService.hash(RAW_REFRESH)).thenReturn(REFRESH_HASH);
		when(authSessionRepository.findByRefreshTokenHash(REFRESH_HASH)).thenReturn(Optional.of(session));

		assertThatThrownBy(() -> authService.refresh(RAW_REFRESH)).isInstanceOf(UnauthorizedException.class);
	}

	@Test
	void shouldRevokeSessionOnLogout() {
		AuthSessionEntity session = new AuthSessionEntity();
		session.setExpiresAt(OffsetDateTime.now().plusDays(1));

		when(refreshTokenService.hash(RAW_REFRESH)).thenReturn(REFRESH_HASH);
		when(authSessionRepository.findByRefreshTokenHash(REFRESH_HASH)).thenReturn(Optional.of(session));

		authService.logout(RAW_REFRESH);

		assertThat(session.getRevokedAt()).isNotNull();
		verify(authSessionRepository).save(session);
	}

	@Test
	void shouldNotFailLogoutWhenTokenUnknown() {
		when(refreshTokenService.hash(RAW_REFRESH)).thenReturn(REFRESH_HASH);
		when(authSessionRepository.findByRefreshTokenHash(REFRESH_HASH)).thenReturn(Optional.empty());

		authService.logout(RAW_REFRESH);

		verify(authSessionRepository, never()).save(any());
	}

	@Test
	void shouldRevokeAllSessionsOnPasswordChange() {
		UUID userId = UUID.randomUUID();
		AuthUserEntity entity = new AuthUserEntity();
		entity.setId(userId);
		AuthUser user = activeUser(userId);

		when(authUserRepository.findByEmail(EMAIL)).thenReturn(Optional.of(entity));
		when(authMapper.entityToDomain(entity)).thenReturn(user);
		when(passwordEncoder.encode("newpassword")).thenReturn("new-hash");

		authService.changePassword(EMAIL, RAW_PASSWORD, "newpassword");

		verify(authValidator).validateChangePasswordBusinessRules(user, RAW_PASSWORD, "newpassword");
		assertThat(entity.getPasswordHash()).isEqualTo("new-hash");
		verify(authUserRepository).save(entity);
		verify(authSessionRepository).revokeAllActiveForUser(eq(userId), any(OffsetDateTime.class));
	}

	@Test
	void shouldLockAccountAndRevokeSessions() {
		UUID userId = UUID.randomUUID();
		AuthUserEntity entity = new AuthUserEntity();
		entity.setId(userId);
		entity.setAccountStatus(AccountStatus.ACTIVE);

		when(authUserRepository.findById(userId)).thenReturn(Optional.of(entity));
		when(authUserRepository.save(entity)).thenReturn(entity);
		when(authMapper.entityToDomain(entity)).thenReturn(activeUser(userId));

		authService.lockAccount(userId);

		assertThat(entity.getAccountStatus()).isEqualTo(AccountStatus.LOCKED);
		verify(authSessionRepository).revokeAllActiveForUser(eq(userId), any(OffsetDateTime.class));
	}
}
