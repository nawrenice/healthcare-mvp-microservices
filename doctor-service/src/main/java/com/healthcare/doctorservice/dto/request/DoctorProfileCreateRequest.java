package com.healthcare.doctorservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DoctorProfileCreateRequest {

    //Required field. Example: Cardiology, Dermatology, etc
    @NotBlank(message = "Specialization is required")
    private String specialization;

    //Required field. Doctor experience cannot be negative.
    @NotNull(message = "Years of experience is required")
    @Min(value = 0, message = "Years of experience cannot be negative")
    private Integer yearsOfExperience;


    //Required field.
    @NotBlank(message = "Qualification is required")
    private String qualification;

    @DecimalMin(value = "0.00", inclusive = true,  message = "Consultation fee cannot be negative")
    private BigDecimal consultationFee;

    private Boolean available;

    private String hospitalName;

    private String department;

}
