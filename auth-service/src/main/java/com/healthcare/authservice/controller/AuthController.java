package com.healthcare.authservice.controller;


import com.healthcare.authservice.dto.response.ApiResponse;
import com.healthcare.authservice.dto.response.AuthResponse;
import com.healthcare.authservice.dto.request.UserLoginRequest;
import com.healthcare.authservice.dto.request.UserSignupRequest;
import com.healthcare.authservice.service.AuthService;
import com.healthcare.authservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller layer exposes HTTP APIs.
 * Keeps business logic in service.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;          //controller delegates business logic to service

    /**
     * Signup endpoint
     * POST /api/auth/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> signup(@Valid @RequestBody UserSignupRequest signupRequest) {
        //Service performs validation/business logic and returns a clean response DTO
        ApiResponse response = userService.signup(signupRequest);

        //201 CREATED  is more appropriate than 200 OK for successful resource creation
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody UserLoginRequest loginRequest){
        //Service verifies email and pw and returns login response
        AuthResponse response = authService.userLogin(loginRequest);

        return ResponseEntity.ok(response);
    }
}
