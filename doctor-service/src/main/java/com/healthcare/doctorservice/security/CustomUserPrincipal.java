package com.healthcare.doctorservice.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CustomUserPrincipal {

    private Long authUserId;

    private String email;

    private String role;
}
