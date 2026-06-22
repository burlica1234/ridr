package com.endava.personal.rider.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.endava.personal.common.exception.NotFoundException;
import com.endava.personal.rider.api.dto.RiderProfileRequestDto;
import com.endava.personal.rider.domain.RiderProfile;
import com.endava.personal.rider.mapper.RiderMapper;
import com.endava.personal.rider.persistence.RiderProfileEntity;
import com.endava.personal.rider.persistence.RiderProfileRepository;
import com.endava.personal.rider.service.validator.RiderValidator;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RiderServiceTest {

	@Mock
	private RiderProfileRepository riderProfileRepository;
	@Mock
	private RiderValidator riderValidator;
	@Mock
	private RiderMapper riderMapper;

	private RiderService riderService;

	@BeforeEach
	void setUp() {
		riderService = new RiderService(riderProfileRepository, riderValidator, riderMapper);
	}

	private RiderProfile profileWithUser(UUID userId) {
		RiderProfile profile = new RiderProfile();
		profile.setUserId(userId);
		profile.setFullName("Alex Rider");
		return profile;
	}

	@Test
	void shouldCreateProfileWithGeneratedIdAndTimestamps() {
		UUID userId = UUID.randomUUID();
		RiderProfile input = profileWithUser(userId);
		RiderProfileEntity entity = new RiderProfileEntity();
		RiderProfileEntity savedEntity = new RiderProfileEntity();
		RiderProfile mapped = profileWithUser(userId);

		when(riderProfileRepository.existsByUserId(userId)).thenReturn(false);
		when(riderMapper.domainToEntity(input)).thenReturn(entity);
		when(riderProfileRepository.save(entity)).thenReturn(savedEntity);
		when(riderMapper.entityToDomain(savedEntity)).thenReturn(mapped);

		RiderProfile result = riderService.createProfile(input);

		assertThat(result).isSameAs(mapped);
		assertThat(input.getId()).isNotNull();
		assertThat(input.getCreatedAt()).isNotNull();
		assertThat(input.getUpdatedAt()).isNotNull();
	}

	@Test
	void shouldThrowWhenProfileNotFoundByUserId() {
		UUID userId = UUID.randomUUID();
		when(riderProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> riderService.getByUserId(userId)).isInstanceOf(NotFoundException.class);
	}

	@Test
	void shouldUpdateProfileAndRefreshUpdatedAt() {
		UUID userId = UUID.randomUUID();
		RiderProfileEntity entity = new RiderProfileEntity();
		RiderProfile mapped = profileWithUser(userId);
		RiderProfileRequestDto request = new RiderProfileRequestDto("New Name", null, null, null, null);

		when(riderProfileRepository.findByUserId(userId)).thenReturn(Optional.of(entity));
		when(riderProfileRepository.save(entity)).thenReturn(entity);
		when(riderMapper.entityToDomain(entity)).thenReturn(mapped);

		RiderProfile result = riderService.updateProfile(userId, request);

		assertThat(result).isSameAs(mapped);
		assertThat(entity.getUpdatedAt()).isNotNull();
	}
}
