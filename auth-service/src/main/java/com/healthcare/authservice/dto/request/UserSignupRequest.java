package com.healthcare.authservice.dto.request;

import com.healthcare.authservice.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


/**
 * DTO (Data Transfer Object) for signup requests.
 * Using DTO avoids exposing internal User entity directly via API.
 */

@Getter
@Setter
public class UserSignupRequest {

    @NotBlank(message = "Name is required") //validation : cannot be blank
    private String name;

    @Email(message = "Invalid email format")    //validates email structure
    @NotBlank(message = "Valid email is required") //validation : must be email
    private String email;

    @NotBlank(message = "Password is required") // cannot be blank
    @Size(min = 12, max = 72, message = "Password must be 12 to 72 characters")
    private String password;

    @NotNull(message = "Role is required")
    private Role role;

}
