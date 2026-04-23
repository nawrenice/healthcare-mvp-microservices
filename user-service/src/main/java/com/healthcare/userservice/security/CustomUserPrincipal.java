package com.healthcare.userservice.security;

import lombok.AllArgsConstructor;
import lombok.Getter;


/*
 * This class represents the authenticated user
 * extracted from JWT and stored in Spring Security context
 */



@Getter
public class CustomUserPrincipal {

    // This comes from JWT (auth-service user id)
    private final Long authUserId;

    // Email stored in JWT (subject)
    private final String email;

    // Role stored in JWT (PATIENT, DOCTOR, etc.)
    private final String role;


    public CustomUserPrincipal(Long authUserId, String email, String role) {
        this.authUserId = authUserId;
        this.email = email;
        this.role = role;
    }

}
