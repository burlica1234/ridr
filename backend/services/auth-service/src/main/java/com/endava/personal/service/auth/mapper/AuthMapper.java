package com.endava.personal.service.auth.mapper;

import com.endava.personal.service.auth.api.dto.AuthUserResponseDto;
import com.endava.personal.service.auth.api.dto.LoginResponseDto;
import com.endava.personal.service.auth.api.dto.RegisterRequestDto;
import com.endava.personal.service.auth.domain.AccountStatus;
import com.endava.personal.service.auth.domain.AuthSession;
import com.endava.personal.service.auth.domain.AuthUser;
import com.endava.personal.service.auth.domain.Role;
import com.endava.personal.service.auth.persistence.AuthUserEntity;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AuthMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "passwordHash", ignore = true)
	@Mapping(target = "role", ignore = true)
	@Mapping(target = "accountStatus", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	AuthUser requestToDomain(RegisterRequestDto request);

	AuthUserResponseDto domainToResponse(AuthUser authUser);

	LoginResponseDto domainToResponse(AuthSession authSession);

	AuthUserEntity domainToEntity(AuthUser authUser);

	AuthUser entityToDomain(AuthUserEntity entity);

	@Mapping(target = "id", source = "id")
	@Mapping(target = "passwordHash", source = "passwordHash")
	@Mapping(target = "role", source = "role")
	@Mapping(target = "accountStatus", source = "accountStatus")
	@Mapping(target = "createdAt", source = "createdAt")
	@Mapping(target = "updatedAt", source = "updatedAt")
	void updateDomainForRegister(UUID id, String passwordHash, Role role, AccountStatus accountStatus,
			OffsetDateTime createdAt, OffsetDateTime updatedAt, @MappingTarget AuthUser authUser);
}
