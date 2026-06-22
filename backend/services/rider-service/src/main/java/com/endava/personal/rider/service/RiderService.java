package com.endava.personal.rider.service;

import com.endava.personal.common.exception.NotFoundException;
import com.endava.personal.rider.api.dto.RiderProfileRequestDto;
import com.endava.personal.rider.domain.RiderProfile;
import com.endava.personal.rider.mapper.RiderMapper;
import com.endava.personal.rider.persistence.RiderProfileEntity;
import com.endava.personal.rider.persistence.RiderProfileRepository;
import com.endava.personal.rider.service.validator.RiderValidator;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RiderService {

	private final RiderProfileRepository riderProfileRepository;
	private final RiderValidator riderValidator;
	private final RiderMapper riderMapper;

	private static final String PROFILE_FOR_USER = "Rider profile for user ";
	private static final String NOT_FOUND = " not found.";

	@Transactional
	public RiderProfile createProfile(RiderProfile riderProfile) {
		riderValidator.validateProfileDoesNotExist(riderProfileRepository.existsByUserId(riderProfile.getUserId()));

		OffsetDateTime now = OffsetDateTime.now();
		riderProfile.setId(UUID.randomUUID());
		riderProfile.setCreatedAt(now);
		riderProfile.setUpdatedAt(now);

		RiderProfileEntity savedEntity = riderProfileRepository.save(riderMapper.domainToEntity(riderProfile));

		return riderMapper.entityToDomain(savedEntity);
	}

	@Transactional(readOnly = true)
	public RiderProfile getByUserId(UUID userId) {
		return riderMapper.entityToDomain(getEntityByUserId(userId));
	}

	@Transactional
	public RiderProfile updateProfile(UUID userId, RiderProfileRequestDto request) {
		RiderProfileEntity entity = getEntityByUserId(userId);

		riderMapper.updateEntity(request, entity);
		entity.setUpdatedAt(OffsetDateTime.now());

		RiderProfileEntity savedEntity = riderProfileRepository.save(entity);

		return riderMapper.entityToDomain(savedEntity);
	}

	private RiderProfileEntity getEntityByUserId(UUID userId) {
		return riderProfileRepository.findByUserId(userId)
				.orElseThrow(() -> new NotFoundException(PROFILE_FOR_USER + userId + NOT_FOUND));
	}
}
