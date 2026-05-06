package com.endava.personal.service.auth.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class AuthUser {

    private UUID id;
    private String passwordHash;
    private String email;
    private AccountStatus accountStatus;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Role role;
}
