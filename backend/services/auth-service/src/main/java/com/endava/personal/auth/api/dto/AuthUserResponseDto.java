package com.endava.personal.auth.api.dto;

import com.endava.personal.auth.domain.AccountStatus;
import com.endava.personal.auth.domain.Role;
import java.util.UUID;

public record AuthUserResponseDto(UUID id, String email, Role role, AccountStatus accountStatus) {
}
