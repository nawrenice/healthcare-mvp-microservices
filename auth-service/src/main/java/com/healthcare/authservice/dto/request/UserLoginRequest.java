package com.healthcare.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


//DTO for login request


@Getter
@Setter
public class UserLoginRequest {
    @Email(message = "Invalid email format")
    @NotBlank(message = "Valid email is required")
    private String email;


    @NotBlank(message = "Password is required")
    private String password;
}
