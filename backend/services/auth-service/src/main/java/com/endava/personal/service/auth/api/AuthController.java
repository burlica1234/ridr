package com.endava.personal.service.auth.api;

import com.endava.personal.service.auth.api.dto.AuthUserResponseDto;
import com.endava.personal.service.auth.api.dto.ChangePasswordRequestDto;
import com.endava.personal.service.auth.api.dto.LoginRequestDto;
import com.endava.personal.service.auth.api.dto.LoginResponseDto;
import com.endava.personal.service.auth.api.dto.RegisterRequestDto;
import com.endava.personal.service.auth.domain.AuthSession;
import com.endava.personal.service.auth.domain.AuthUser;
import com.endava.personal.service.auth.mapper.AuthMapper;
import com.endava.personal.service.auth.service.AuthService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final AuthMapper authMapper;

	@PostMapping("/register")
	public ResponseEntity<AuthUserResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
		AuthUser authUser = authMapper.requestToDomain(request);
		AuthUser createdAuthUser = authService.register(authUser, request.password());

		return ResponseEntity.status(HttpStatus.CREATED).body(authMapper.domainToResponse(createdAuthUser));
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
		AuthSession authSession = authService.login(request.email(), request.password());
		return ResponseEntity.status(HttpStatus.OK).body(authMapper.domainToResponse(authSession));
	}

	@GetMapping("/me")
	public ResponseEntity<AuthUserResponseDto> getCurrentUser(Principal principal) {
        AuthUser authUser = authService.getCurrentUser(principal.getName());

        return ResponseEntity.status(HttpStatus.OK).body(authMapper.domainToResponse(authUser));
	}

	@GetMapping("/{id}")
	public ResponseEntity<AuthUserResponseDto> getById(@PathVariable UUID id) {
		AuthUser authUser = authService.getById(id);

		return ResponseEntity.ok(authMapper.domainToResponse(authUser));
	}

	@PostMapping("/{id}/change-password")
	public ResponseEntity<Void> changePassword(@PathVariable UUID id,
			@Valid @RequestBody ChangePasswordRequestDto request) {
		authService.changePassword(id, request.currentPassword(), request.newPassword());

		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/{id}/lock")
	public ResponseEntity<AuthUserResponseDto> lockAccount(@PathVariable UUID id) {
		AuthUser authUser = authService.lockAccount(id);

		return ResponseEntity.status(HttpStatus.OK).body(authMapper.domainToResponse(authUser));
	}

	@PatchMapping("/{id}/unlock")
	public ResponseEntity<AuthUserResponseDto> unlockAccount(@PathVariable UUID id) {
		AuthUser authUser = authService.unlockAccount(id);

		return ResponseEntity.status(HttpStatus.OK).body(authMapper.domainToResponse(authUser));
	}

	@PatchMapping("/{id}/deactivate")
	public ResponseEntity<AuthUserResponseDto> deactivateAccount(@PathVariable UUID id) {
		AuthUser authUser = authService.deactivateAccount(id);

		return ResponseEntity.status(HttpStatus.OK).body(authMapper.domainToResponse(authUser));
	}
}
