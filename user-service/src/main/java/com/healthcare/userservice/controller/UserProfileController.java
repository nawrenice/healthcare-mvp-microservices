package com.healthcare.userservice.controller;

import com.healthcare.userservice.dto.request.CreateUserProfileRequest;
import com.healthcare.userservice.dto.request.UpdateUserProfileRequest;
import com.healthcare.userservice.dto.response.UserProfileResponse;
import com.healthcare.userservice.security.CustomUserPrincipal;
import com.healthcare.userservice.service.UserProfileService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;



/*
 * Controller layer
 *
 * Responsibility:
 * - Accept HTTP requests
 * - Validate request body
 * - Get logged-in user info from JWT
 * - Call service layer
 * - Return HTTP response
 *
 * Important:
 * We DO NOT trust authUserId from request body anymore.
 * We only take authUserId from JWT using @AuthenticationPrincipal.
 */


@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/user-profiles")
@RequiredArgsConstructor
public class UserProfileController {

    // Service layer bean that contains business logic
    private final UserProfileService userProfileService;

    /*
     * Create profile for the currently logged-in user
     *
     * Endpoint:
     * POST /api/user-profiles
     *
     * Flow:
     * 1. Request comes with JWT token
     * 2. JwtAuthenticationFilter validates token
     * 3. Filter puts CustomUserPrincipal into SecurityContext
     * 4. Controller receives principal using @AuthenticationPrincipal
     * 5. We extract authUserId from JWT
     * 6. Pass authUserId + request body to service
     *
     * Why this is secure:
     * - Client cannot send fake authUserId
     * - Identity always comes from JWT
     */


    /*
     * Only PATIENT users can CREATE their own profile
     */
    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping
    public ResponseEntity<UserProfileResponse> createProfile(
            @Valid @RequestBody CreateUserProfileRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        // Extract authenticated user id from JWT
        Long authUserId = principal.getAuthUserId();

        // Call service to create profile for logged-in user
        UserProfileResponse response = userProfileService.createProfile(authUserId, request);

        // Return HTTP 201 Created
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /*
     * Get profile of currently logged-in user
     *
     * Endpoint:
     * GET /api/user-profiles/me
     *
     * Why "/me"?
     * - Cleaner API design
     * - User does not need to send their id
     * - Backend already knows user from JWT
     */


    /*
     * Only PATIENT users can FETCH their own profile
     */
    @PreAuthorize(("hasRole('PATIENT')"))
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        // Extract authenticated user id from JWT
        Long authUserId = principal.getAuthUserId();

        // Fetch profile for logged-in user
        UserProfileResponse response = userProfileService.getProfileByAuthUserId(authUserId);

        // Return HTTP 200 OK
        return ResponseEntity.ok(response);
    }

    /*
     * Update profile of currently logged-in user
     *
     * Endpoint:
     * PUT /api/user-profiles/me
     *
     * Flow:
     * - Read logged-in user's authUserId from JWT
     * - Use request body only for editable profile fields
     * - Service updates only that user's profile
     */

    /*
     * Only PATIENT users can UPDATE their own profile
     */
    @PreAuthorize("hasRole('PATIENT')")
    @PatchMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            @Valid @RequestBody UpdateUserProfileRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        // Extract authenticated user id from JWT
        Long authUserId = principal.getAuthUserId();

        // Update profile for logged-in user
        UserProfileResponse response = userProfileService.updateProfile(authUserId, request);

        // Return updated profile
        return ResponseEntity.ok(response);
    }

}
