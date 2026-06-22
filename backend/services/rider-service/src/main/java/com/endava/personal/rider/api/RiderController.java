package com.endava.personal.rider.api;

import com.endava.personal.common.security.AuthPrincipal;
import com.endava.personal.rider.api.dto.RiderProfileRequestDto;
import com.endava.personal.rider.api.dto.RiderProfileResponseDto;
import com.endava.personal.rider.domain.RiderProfile;
import com.endava.personal.rider.mapper.RiderMapper;
import com.endava.personal.rider.service.RiderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/riders")
@RequiredArgsConstructor
public class RiderController {

	private final RiderService riderService;
	private final RiderMapper riderMapper;

	@PostMapping
	public ResponseEntity<RiderProfileResponseDto> createMyProfile(@AuthenticationPrincipal AuthPrincipal principal,
			@Valid @RequestBody RiderProfileRequestDto request) {
		RiderProfile profile = riderMapper.requestToDomain(request);
		profile.setUserId(principal.userId());

		RiderProfile created = riderService.createProfile(profile);

		return ResponseEntity.status(HttpStatus.CREATED).body(riderMapper.domainToResponse(created));
	}

	@GetMapping("/me")
	public ResponseEntity<RiderProfileResponseDto> getMyProfile(@AuthenticationPrincipal AuthPrincipal principal) {
		RiderProfile profile = riderService.getByUserId(principal.userId());

		return ResponseEntity.ok(riderMapper.domainToResponse(profile));
	}

	@PutMapping("/me")
	public ResponseEntity<RiderProfileResponseDto> updateMyProfile(@AuthenticationPrincipal AuthPrincipal principal,
			@Valid @RequestBody RiderProfileRequestDto request) {
		RiderProfile profile = riderService.updateProfile(principal.userId(), request);

		return ResponseEntity.ok(riderMapper.domainToResponse(profile));
	}
}
