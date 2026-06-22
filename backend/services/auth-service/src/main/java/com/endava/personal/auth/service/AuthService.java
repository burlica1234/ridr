package com.endava.personal.auth.service;

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
import com.endava.personal.common.exception.NotFoundException;
import com.endava.personal.common.exception.UnauthorizedException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final AuthUserRepository authUserRepository;
	private final AuthSessionRepository authSessionRepository;
	private final AuthValidator authValidator;
	private final AuthMapper authMapper;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final RefreshTokenService refreshTokenService;
	private final JwtProperties jwtProperties;

	private static final String AUTH_USER_WITH_ID = "Auth user with id ";
	private static final String AUTH_USER_WITH_EMAIL = "Auth user with email ";
	private static final String NOT_FOUND = " not found.";
	private static final String INVALID_CREDENTIALS = "Invalid credentials.";
	private static final String INVALID_REFRESH_TOKEN = "Invalid or expired refresh token.";
	private static final String TOKEN_TYPE = "Bearer";

	@Transactional
	public AuthUser register(AuthUser authUser, String rawPassword) {
		authValidator.validateRegisterBusinessRules(authUser, rawPassword,
				authUserRepository.existsByEmail(authUser.getEmail()));

		enrichForRegistration(authUser, rawPassword);

		AuthUserEntity entity = authMapper.domainToEntity(authUser);
		AuthUserEntity savedEntity = authUserRepository.save(entity);

		return authMapper.entityToDomain(savedEntity);
	}

	@Transactional
	public AuthTokens login(String email, String rawPassword) {
		AuthUserEntity entity = authUserRepository.findByEmail(email)
				.orElseThrow(() -> new UnauthorizedException(INVALID_CREDENTIALS));

		AuthUser authUser = authMapper.entityToDomain(entity);
		authValidator.validateCredentials(authUser, rawPassword);

		return issueTokens(authUser);
	}

	@Transactional
	public AuthTokens refresh(String rawRefreshToken) {
		String tokenHash = refreshTokenService.hash(rawRefreshToken);
		OffsetDateTime now = OffsetDateTime.now();

		AuthSessionEntity session = authSessionRepository.findByRefreshTokenHash(tokenHash)
				.orElseThrow(() -> new UnauthorizedException(INVALID_REFRESH_TOKEN));

		if (session.getRevokedAt() != null || !session.getExpiresAt().isAfter(now)) {
			throw new UnauthorizedException(INVALID_REFRESH_TOKEN);
		}

		AuthUser authUser = getById(session.getUserId());
		authValidator.validateAccountStatus(authUser);

		session.setRevokedAt(now);
		authSessionRepository.save(session);

		return issueTokens(authUser);
	}

	@Transactional
	public void logout(String rawRefreshToken) {
		String tokenHash = refreshTokenService.hash(rawRefreshToken);

		authSessionRepository.findByRefreshTokenHash(tokenHash).ifPresent(session -> {
			if (session.getRevokedAt() == null) {
				session.setRevokedAt(OffsetDateTime.now());
				authSessionRepository.save(session);
			}
		});
	}

	@Transactional(readOnly = true)
	public AuthUser getById(UUID id) {
		AuthUserEntity entity = authUserRepository.findById(id)
				.orElseThrow(() -> new NotFoundException(AUTH_USER_WITH_ID + id + NOT_FOUND));

		return authMapper.entityToDomain(entity);
	}

	@Transactional(readOnly = true)
	public AuthUser getByEmail(String email) {
		AuthUserEntity entity = authUserRepository.findByEmail(email)
				.orElseThrow(() -> new NotFoundException(AUTH_USER_WITH_EMAIL + email + NOT_FOUND));

		return authMapper.entityToDomain(entity);
	}

	@Transactional(readOnly = true)
	public AuthUser getCurrentUser(String email) {
		AuthUser authUser = getByEmail(email);
		authValidator.validateAccountStatus(authUser);

		return authUser;
	}

	@Transactional(readOnly = true)
	public AuthUserEntity getEntityById(UUID id) {
		return authUserRepository.findById(id)
				.orElseThrow(() -> new NotFoundException(AUTH_USER_WITH_ID + id + NOT_FOUND));
	}

	@Transactional
	public void changePassword(String email, String currentPassword, String newPassword) {
		AuthUserEntity entity = authUserRepository.findByEmail(email)
				.orElseThrow(() -> new NotFoundException(AUTH_USER_WITH_EMAIL + email + NOT_FOUND));
		AuthUser authUser = authMapper.entityToDomain(entity);

		authValidator.validateChangePasswordBusinessRules(authUser, currentPassword, newPassword);

		entity.setPasswordHash(passwordEncoder.encode(newPassword));
		entity.setUpdatedAt(OffsetDateTime.now());

		authUserRepository.save(entity);

		// Force re-authentication everywhere after a password change.
		authSessionRepository.revokeAllActiveForUser(entity.getId(), OffsetDateTime.now());
	}

	@Transactional
	public AuthUser lockAccount(UUID id) {
		AuthUser authUser = updateAccountStatus(id, AccountStatus.LOCKED);
		authSessionRepository.revokeAllActiveForUser(id, OffsetDateTime.now());

		return authUser;
	}

	@Transactional
	public AuthUser unlockAccount(UUID id) {
		return updateAccountStatus(id, AccountStatus.ACTIVE);
	}

	@Transactional
	public AuthUser deactivateAccount(UUID id) {
		AuthUser authUser = updateAccountStatus(id, AccountStatus.INACTIVE);
		authSessionRepository.revokeAllActiveForUser(id, OffsetDateTime.now());

		return authUser;
	}

	@Transactional(readOnly = true)
	public boolean existsById(UUID id) {
		return authUserRepository.existsById(id);
	}

	@Transactional(readOnly = true)
	public boolean existsByEmail(String email) {
		return authUserRepository.existsByEmail(email);
	}

	@Transactional(readOnly = true)
	public AuthUserEntity getEntityByEmail(String email) {
		return authUserRepository.findByEmail(email)
				.orElseThrow(() -> new NotFoundException(AUTH_USER_WITH_EMAIL + email + NOT_FOUND));
	}

	private AuthUser updateAccountStatus(UUID id, AccountStatus status) {
		AuthUserEntity entity = getEntityById(id);

		entity.setAccountStatus(status);
		entity.setUpdatedAt(OffsetDateTime.now());

		AuthUserEntity savedEntity = authUserRepository.save(entity);
		return authMapper.entityToDomain(savedEntity);
	}

	private AuthTokens issueTokens(AuthUser authUser) {
        String accessToken = jwtService.generateToken(authUser.getId(), authUser.getEmail(), authUser.getRole().name());
		String rawRefreshToken = createRefreshSession(authUser.getId());
		long expiresInSeconds = jwtProperties.expirationMs() / 1000;

		return new AuthTokens(accessToken, rawRefreshToken, TOKEN_TYPE, expiresInSeconds);
	}

	private String createRefreshSession(UUID userId) {
		OffsetDateTime now = OffsetDateTime.now();
		String rawRefreshToken = refreshTokenService.generateRawToken();

		AuthSessionEntity session = new AuthSessionEntity();
		session.setId(UUID.randomUUID());
		session.setUserId(userId);
		session.setRefreshTokenHash(refreshTokenService.hash(rawRefreshToken));
		session.setExpiresAt(now.plus(Duration.ofMillis(jwtProperties.refreshExpirationMs())));
		session.setCreatedAt(now);

		authSessionRepository.save(session);

		return rawRefreshToken;
	}

	private void enrichForRegistration(AuthUser authUser, String rawPassword) {
		OffsetDateTime now = OffsetDateTime.now();

		authMapper.updateDomainForRegister(UUID.randomUUID(), passwordEncoder.encode(rawPassword), Role.RIDER,
				AccountStatus.ACTIVE, now, now, authUser);
	}
}
