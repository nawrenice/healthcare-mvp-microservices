package com.healthcare.authservice.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@SecurityRequirement(name = "bearerAuth")
public class TestController {

    @GetMapping("/me")
    public String getCurrentUser(Authentication authentication){
        if(authentication == null){
            return "No authenticated user.";
        }
        return "Authenticated as: " +authentication.getName();
    }

    @GetMapping("/patient")
    @PreAuthorize(("hasRole('PATIENT')"))
    public String patientOnly(){
        return "welcome PATIENT..";
    }

    @GetMapping("/doctor")
    @PreAuthorize(("hasRole('DOCTOR')"))
    public String doctorOnly(){
        return "welcome DOCTOR..";
    }

    @GetMapping("/admin")
    @PreAuthorize(("hasRole('ADMIN')"))
    public String adminOnly(){
        return "welcome ADMIN..";
    }

    @GetMapping("/reception")
    @PreAuthorize(("hasRole('RECEPTION')"))
    public String receptionOnly(){
        return "welcome RECEPTION..";
    }
}
