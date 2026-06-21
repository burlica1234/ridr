package com.endava.personal.service.auth.api.dto;

import com.endava.personal.service.auth.domain.AccountStatus;
import com.endava.personal.service.auth.domain.Role;
import java.util.UUID;

public record AuthUserResponseDto(UUID id, String email, Role role, AccountStatus accountStatus) {
}
