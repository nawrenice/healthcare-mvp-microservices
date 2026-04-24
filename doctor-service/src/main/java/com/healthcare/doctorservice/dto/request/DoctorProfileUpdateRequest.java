package com.healthcare.doctorservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import java.math.BigDecimal;

@Getter
@Setter
public class DoctorProfileUpdateRequest {

    @NotBlank(message = "Specialization is required")
    private String specialization;


    @NotNull(message = "Years of experience is required")
    @Min(value =0, message = "Years of experience cannot be negative")
    private Integer yearsOfExperience;

    @NotBlank(message = "Qualification is required")
    private String qualification;

    @DecimalMin(value = "0.00", inclusive = true, message = "Consultation fee cannot be negative")
    private BigDecimal consultationFee;

    // Optional availability flag
    private Boolean available;

    // Optional hospital name
    private String hospitalName;

    // Optional department name
    private String department;

}
