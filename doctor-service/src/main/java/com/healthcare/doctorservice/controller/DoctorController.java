package com.healthcare.doctorservice.controller;

import com.healthcare.doctorservice.dto.request.DoctorProfileCreateRequest;
import com.healthcare.doctorservice.dto.response.DoctorProfileResponse;
import com.healthcare.doctorservice.dto.request.DoctorProfileUpdateRequest;
import com.healthcare.doctorservice.security.CustomUserPrincipal;
import com.healthcare.doctorservice.service.DoctorService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * ============================================================
 *                     DOCTOR CONTROLLER
 * ============================================================
 *
 * Request Flow:
 *
 * Client sends HTTP request with JWT
 *          │
 *          ▼
 * SecurityConfig
 *          │
 *          ▼
 * JwtAuthenticationFilter
 *          │
 *          ├── Validates JWT
 *          ├── Creates CustomUserPrincipal
 *          └── Stores authenticated user in Spring Security
 *          │
 *          ▼
 * DoctorController  ← This class
 *          │
 *          ├── Receives request
 *          ├── Validates request data
 *          ├── Gets authUserId from authenticated principal
 *          └── Calls DoctorService
 *          │
 *          ▼
 * DoctorService
 *          │
 *          ▼
 * DoctorRepository
 *          │
 *          ▼
 * Doctor Database
 *
 * ============================================================
 *
 * What is the responsibility of this class?
 *
 * This class exposes REST API endpoints for doctor-related
 * operations.
 *
 * It accepts HTTP requests, gets the authenticated user's
 * identity, calls the service layer, and returns HTTP responses.
 *
 * Important:
 *
 * This controller does not contain business logic.
 *
 * Business rules belong inside DoctorService.
 *
 * The controller also never accepts authUserId from the client.
 * It receives authUserId from the verified JWT through
 * CustomUserPrincipal.
 */
@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class DoctorController {

    private final DoctorService doctorService;

    /**
     * What does this method do?
     *
     * Creates a doctor profile for the currently authenticated doctor.
     *
     * Why do we need this?
     *
     * A user may already have an account in Auth Service, but the
     * doctor-specific information belongs inside Doctor Service.
     *
     * Examples of doctor-specific information:
     *
     * - specialization
     * - qualification
     * - consultation fee
     * - hospital name
     * - department
     * - availability
     *
     * Real-life analogy:
     *
     * Creating an account is like receiving an employee ID number.
     * Creating a doctor profile is like completing the doctor's
     * professional record in the hospital system.
     *
     * Important rule to remember:
     *
     * authUserId comes from the verified JWT.
     * It must never come from the request body.
     */
    @PostMapping("/profile")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorProfileResponse> createDoctorProfile(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody DoctorProfileCreateRequest request
    ) {

        Long authUserId = principal.getAuthUserId();

        DoctorProfileResponse response =
                doctorService.createDoctorProfile(authUserId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * What does this method do?
     *
     * Returns the doctor profile belonging to the currently
     * authenticated doctor.
     *
     * Why do we need this?
     *
     * A logged-in doctor should be able to view their own profile
     * without manually providing their ID.
     *
     * Real-life analogy:
     *
     * Think of an employee opening the "My Profile" section.
     * The system already knows which employee is logged in.
     *
     * Important rule to remember:
     *
     * We search using authUserId from the authenticated principal,
     * not an ID supplied by the client.
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorProfileResponse> getDoctorProfile(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {

        Long authUserId = principal.getAuthUserId();

        DoctorProfileResponse response =
                doctorService.getDoctorProfile(authUserId);

        return ResponseEntity.ok(response);
    }

    /**
     * What does this method do?
     *
     * Updates the profile belonging to the currently authenticated
     * doctor.
     *
     * Why do we need this?
     *
     * Doctors may need to update information such as:
     *
     * - consultation fee
     * - availability
     * - hospital name
     * - department
     * - qualification
     *
     * Real-life analogy:
     *
     * This is similar to a doctor updating their professional
     * information in the hospital employee portal.
     *
     * Important rule to remember:
     *
     * The authenticated doctor can update only the profile connected
     * to their own authUserId.
     */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorProfileResponse> updateDoctorProfile(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody DoctorProfileUpdateRequest request
    ) {

        Long authUserId = principal.getAuthUserId();

        DoctorProfileResponse response =
                doctorService.updateDoctorProfile(authUserId, request);

        return ResponseEntity.ok(response);
    }

    /**
     * What does this method do?
     *
     * Returns all doctor profiles stored in Doctor Service.
     *
     * Why do we need this?
     *
     * Patients, receptionists, or administrators may need to view
     * the doctors registered in the healthcare platform.
     *
     * Real-life analogy:
     *
     * Think of this as opening the hospital's complete doctor
     * directory.
     *
     * Important rule to remember:
     *
     * This endpoint returns response DTOs, not Doctor entities.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('PATIENT', 'RECEPTIONIST', 'ADMIN', 'DOCTOR')")
    public ResponseEntity<List<DoctorProfileResponse>> getAllDoctors() {

        List<DoctorProfileResponse> doctors =
                doctorService.getAllDoctors();

        return ResponseEntity.ok(doctors);
    }

    /**
     * What does this method do?
     *
     * Returns only doctors whose availability is currently set
     * to true.
     *
     * Why do we need this?
     *
     * Patients and receptionists generally need to see doctors
     * who are currently accepting appointments.
     *
     * Real-life analogy:
     *
     * A hospital directory may show every doctor, while an
     * appointment screen should show only doctors who are available.
     *
     * Important rule to remember:
     *
     * The filtering responsibility belongs in the repository and
     * service layers, not inside the controller.
     */
    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('PATIENT', 'RECEPTIONIST', 'ADMIN', 'DOCTOR')")
    public ResponseEntity<List<DoctorProfileResponse>> getAvailableDoctors() {

        List<DoctorProfileResponse> doctors =
                doctorService.getAvailableDoctors();

        return ResponseEntity.ok(doctors);
    }
}


/* *********** ENDPOINT SUMMARY ************
* POST   /api/doctors/profile
       → Create logged-in doctor's profile
       → DOCTOR only
       → Returns 201 Created

GET    /api/doctors/profile
       → Get logged-in doctor's profile
       → DOCTOR only
       → Returns 200 OK

PUT    /api/doctors/profile
       → Update logged-in doctor's profile
       → DOCTOR only
       → Returns 200 OK

GET    /api/doctors
       → Get all doctors
       → Authenticated healthcare roles
       → Returns 200 OK

GET    /api/doctors/available
       → Get available doctors
       → Authenticated healthcare roles
       → Returns 200 OK*/