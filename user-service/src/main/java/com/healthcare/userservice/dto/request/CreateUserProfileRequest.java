package com.healthcare.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateUserProfileRequest {

    // Identity comes from the verified JWT, never from editable request fields.

    @NotBlank(message = "fullName is required")
    @jakarta.validation.constraints.Size(max = 255)
    private String name;

    @Pattern(
            regexp = "^[0-9]{10,15}$",
            message = "phone must contain only digits and be 10 to 15 characters long"
    )
    private String phone;

    @NotBlank(message = "address is required")
    @jakarta.validation.constraints.Size(max = 255)
    private String address;

    @Past(message = "dateOfBirth must be a past date")
    private LocalDate dateOfBirth;

    @NotBlank(message = "gender is required")
    @jakarta.validation.constraints.Size(max = 255)
    private String gender;

}
