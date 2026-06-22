package com.endava.personal.rider.mapper;

import com.endava.personal.rider.api.dto.RiderProfileRequestDto;
import com.endava.personal.rider.api.dto.RiderProfileResponseDto;
import com.endava.personal.rider.domain.RiderProfile;
import com.endava.personal.rider.persistence.RiderProfileEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface RiderMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "userId", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	RiderProfile requestToDomain(RiderProfileRequestDto request);

	RiderProfileEntity domainToEntity(RiderProfile riderProfile);

	RiderProfile entityToDomain(RiderProfileEntity entity);

	RiderProfileResponseDto domainToResponse(RiderProfile riderProfile);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "userId", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	void updateEntity(RiderProfileRequestDto request, @MappingTarget RiderProfileEntity entity);
}
