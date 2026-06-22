package com.endava.personal.common.security;

import java.security.Principal;
import java.util.UUID;

public record AuthPrincipal(UUID userId, String email, String role) implements Principal {

	@Override
	public String getName() {
		return email;
	}
}
