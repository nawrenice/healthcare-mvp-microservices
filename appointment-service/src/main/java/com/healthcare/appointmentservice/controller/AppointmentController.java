package com.healthcare.appointmentservice.controller;

import com.healthcare.appointmentservice.dto.request.BookAppointmentRequest;
import com.healthcare.appointmentservice.dto.response.AppointmentResponse;
import com.healthcare.appointmentservice.security.CustomUserPrincipal;
import com.healthcare.appointmentservice.service.AppointmentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

@RestController @RequiredArgsConstructor
@RequestMapping("/api/appointments")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
public class AppointmentController {
    private final AppointmentService appointments;

    @PostMapping @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentResponse> book(@AuthenticationPrincipal CustomUserPrincipal user,
            @RequestHeader("Authorization") String authorization, @Valid @RequestBody BookAppointmentRequest request) {
        return ResponseEntity.status(201).body(appointments.book(user.getAuthUserId(), request, authorization));
    }
    @GetMapping("/me")
    public Page<AppointmentResponse> mine(@AuthenticationPrincipal CustomUserPrincipal user,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return appointments.mine(user, page, size);
    }
    @GetMapping("/{id}")
    public AppointmentResponse get(@PathVariable Long id, @AuthenticationPrincipal CustomUserPrincipal user) {
        return appointments.get(id, user);
    }
    @PatchMapping("/{id}/cancel")
    public AppointmentResponse cancel(@PathVariable Long id, @AuthenticationPrincipal CustomUserPrincipal user) {
        return appointments.cancel(id, user);
    }
    @PatchMapping("/{id}/complete") @PreAuthorize("hasRole('DOCTOR')")
    public AppointmentResponse complete(@PathVariable Long id, @AuthenticationPrincipal CustomUserPrincipal user) {
        return appointments.complete(id, user);
    }
}
