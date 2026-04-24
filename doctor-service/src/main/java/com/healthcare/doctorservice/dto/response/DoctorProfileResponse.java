package com.healthcare.doctorservice.dto.response;


import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoctorProfileResponse {

    // Local doctor-service profile ID
    private Long id;

    // User ID from auth-service, extracted from JWT
    private Long authUserId;

    private String specialization;

    private Integer yearsOfExperience;

    private String qualification;

    private BigDecimal consultationFee;

    private Boolean available;

    private String hospitalName;

    private String department;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
