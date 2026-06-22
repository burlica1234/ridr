package com.endava.personal.service.auth.service;

import com.endava.personal.common.exception.NotFoundException;
import com.endava.personal.common.exception.UnauthorizedException;
import com.endava.personal.service.auth.domain.AccountStatus;
import com.endava.personal.service.auth.domain.AuthSession;
import com.endava.personal.service.auth.domain.AuthUser;
import com.endava.personal.service.auth.domain.Role;
import com.endava.personal.service.auth.mapper.AuthMapper;
import com.endava.personal.service.auth.persistence.AuthUserEntity;
import com.endava.personal.service.auth.persistence.AuthUserRepository;
import com.endava.personal.service.auth.service.validator.AuthValidator;
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
	private final AuthValidator authValidator;
	private final AuthMapper authMapper;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	private static final String AUTH_USER_WITH_ID = "Auth user with id ";
	private static final String AUTH_USER_WITH_EMAIL = "Auth user with email ";
	private static final String NOT_FOUND = " not found.";
	private static final String INVALID_CREDENTIALS = "Invalid credentials.";
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

	@Transactional(readOnly = true)
	public AuthSession login(String email, String rawPassword) {
		AuthUserEntity entity = authUserRepository.findByEmail(email)
				.orElseThrow(() -> new UnauthorizedException(INVALID_CREDENTIALS));

		AuthUser authUser = authMapper.entityToDomain(entity);
		authValidator.validateCredentials(authUser, rawPassword);

        String accessToken = jwtService.generateToken(authUser.getEmail(), authUser.getRole().name());

		return new AuthSession(accessToken, TOKEN_TYPE);
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
	public void changePassword(UUID userId, String currentPassword, String newPassword) {
		AuthUserEntity entity = getEntityById(userId);
		AuthUser authUser = authMapper.entityToDomain(entity);

		authValidator.validateChangePasswordBusinessRules(authUser, currentPassword, newPassword);

		entity.setPasswordHash(passwordEncoder.encode(newPassword));
		entity.setUpdatedAt(OffsetDateTime.now());

		authUserRepository.save(entity);
	}

	@Transactional
	public AuthUser lockAccount(UUID id) {
		AuthUserEntity entity = getEntityById(id);

		entity.setAccountStatus(AccountStatus.LOCKED);
		entity.setUpdatedAt(OffsetDateTime.now());

		AuthUserEntity savedEntity = authUserRepository.save(entity);
		return authMapper.entityToDomain(savedEntity);
	}

	@Transactional
	public AuthUser unlockAccount(UUID id) {
		AuthUserEntity entity = getEntityById(id);

		entity.setAccountStatus(AccountStatus.ACTIVE);
		entity.setUpdatedAt(OffsetDateTime.now());

		AuthUserEntity savedEntity = authUserRepository.save(entity);
		return authMapper.entityToDomain(savedEntity);
	}

	@Transactional
	public AuthUser deactivateAccount(UUID id) {
		AuthUserEntity entity = getEntityById(id);

		entity.setAccountStatus(AccountStatus.INACTIVE);
		entity.setUpdatedAt(OffsetDateTime.now());

		AuthUserEntity savedEntity = authUserRepository.save(entity);
		return authMapper.entityToDomain(savedEntity);
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

	private void enrichForRegistration(AuthUser authUser, String rawPassword) {
		OffsetDateTime now = OffsetDateTime.now();

		authMapper.updateDomainForRegister(UUID.randomUUID(), passwordEncoder.encode(rawPassword), Role.RIDER,
				AccountStatus.ACTIVE, now, now, authUser);
	}
}
