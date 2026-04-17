package com.healthcare.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateUserProfileRequest {

    @NotBlank(message = "fullName is required")
    private String name;

    @Pattern(
            regexp = "^[0-9]{10,15}$",
            message = "phone must contain only digits and be 10 to 15 characters long"
    )
    private String phone;

    @NotBlank(message = "address is required")
    private String address;

    @Past(message = "dateOfBirth must be a past date")
    private LocalDate dateOfBirth;

    @NotBlank(message = "gender is required")
    private String gender;

}
