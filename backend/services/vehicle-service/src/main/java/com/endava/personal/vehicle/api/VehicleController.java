package com.endava.personal.vehicle.api;

import com.endava.personal.common.security.AuthPrincipal;
import com.endava.personal.vehicle.api.dto.CreateUserVehicleRequestDto;
import com.endava.personal.vehicle.api.dto.UserVehicleResponseDto;
import com.endava.personal.vehicle.api.dto.VehicleTypeResponseDto;
import com.endava.personal.vehicle.domain.UserVehicle;
import com.endava.personal.vehicle.mapper.VehicleMapper;
import com.endava.personal.vehicle.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;
    private final VehicleMapper vehicleMapper;

    @GetMapping("/types")
    public ResponseEntity<List<VehicleTypeResponseDto>> getVehicleTypes(){
        List<VehicleTypeResponseDto> types = vehicleService.getVehicleTypes()
                .stream()
                .map(vehicleMapper::domainToResponse)
                .toList();
        return ResponseEntity.ok(types);
    }

    @GetMapping("/my")
    public ResponseEntity<List<UserVehicleResponseDto>> getMyVehicles(@AuthenticationPrincipal AuthPrincipal authPrincipal){
        List<UserVehicleResponseDto> vehicles = vehicleService.getVehiclesForUser(authPrincipal.userId())
                .stream()
                .map(vehicleMapper::domainToResponse)
                .toList();
        return ResponseEntity.ok(vehicles);
    }

    @PostMapping
    public ResponseEntity<UserVehicleResponseDto> addVehicle(@AuthenticationPrincipal AuthPrincipal principal,
                                                             @Valid @RequestBody CreateUserVehicleRequestDto request) {
        UserVehicle userVehicle = vehicleMapper.requestToDomain(request);
        userVehicle.setUserId(principal.userId());

        UserVehicle created = vehicleService.addVehicle(userVehicle);

        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleMapper.domainToResponse(created));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID id) {
        vehicleService.deleteVehicle(principal.userId(), id);

        return ResponseEntity.noContent().build();
    }
}
