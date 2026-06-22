package com.endava.personal.auth.mapper;

import com.endava.personal.auth.api.dto.AuthUserResponseDto;
import com.endava.personal.auth.api.dto.LoginResponseDto;
import com.endava.personal.auth.api.dto.RegisterRequestDto;
import com.endava.personal.auth.domain.AccountStatus;
import com.endava.personal.auth.domain.AuthTokens;
import com.endava.personal.auth.domain.AuthUser;
import com.endava.personal.auth.domain.Role;
import com.endava.personal.auth.persistence.AuthUserEntity;
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

	@Mapping(target = "expiresIn", source = "expiresInSeconds")
	LoginResponseDto domainToResponse(AuthTokens authTokens);

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
