package com.healthcare.doctorservice.controller;

import com.healthcare.doctorservice.dto.request.DoctorProfileRequest;
import com.healthcare.doctorservice.dto.response.DoctorProfileResponse;
import com.healthcare.doctorservice.security.CustomUserPrincipal;
import com.healthcare.doctorservice.service.DoctorService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

@RestController @RequiredArgsConstructor
@RequestMapping("/api/doctors")
@SecurityRequirement(name = "bearerAuth")
public class DoctorController {
    private final DoctorService doctors;

    @PostMapping @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorProfileResponse> create(@AuthenticationPrincipal CustomUserPrincipal user,
                                                       @Valid @RequestBody DoctorProfileRequest request) {
        return ResponseEntity.status(201).body(doctors.create(user.getAuthUserId(), request));
    }
    @GetMapping("/me") @PreAuthorize("hasRole('DOCTOR')")
    public DoctorProfileResponse mine(@AuthenticationPrincipal CustomUserPrincipal user) {
        return doctors.getMine(user.getAuthUserId());
    }
    @PutMapping("/me") @PreAuthorize("hasRole('DOCTOR')")
    public DoctorProfileResponse update(@AuthenticationPrincipal CustomUserPrincipal user,
                                        @Valid @RequestBody DoctorProfileRequest request) {
        return doctors.update(user.getAuthUserId(), request);
    }
    @GetMapping("/{id}")
    public DoctorProfileResponse get(@PathVariable Long id) { return doctors.get(id); }
    @GetMapping
    public Page<DoctorProfileResponse> list(@RequestParam(defaultValue = "true") boolean availableOnly,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        return doctors.list(availableOnly, page, size);
    }
}
