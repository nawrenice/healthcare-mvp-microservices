package com.healthcare.doctorservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DoctorProfileRequest {
    @NotBlank @Size(max = 120) private String name;
    @NotBlank @Size(max = 120) private String specialty;
    @NotBlank @Size(max = 80) private String licenseNumber;
    @NotBlank @Size(max = 200) private String clinicAddress;
    // False until the doctor explicitly chooses to accept appointments.
    private boolean available;
}
