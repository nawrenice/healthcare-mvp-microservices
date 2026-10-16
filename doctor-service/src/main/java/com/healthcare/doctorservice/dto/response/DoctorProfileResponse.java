package com.healthcare.doctorservice.dto.response;

import lombok.Builder;
import lombok.Getter;

@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@Getter @Builder(toBuilder = true)
public class DoctorProfileResponse {
    private Long id;
    private Long authUserId;
    private String name;
    private String specialty;
    private String clinicAddress;
    // Only populated for the owning doctor; omitted from public directory responses.
    private String licenseNumber;
    private boolean available;
    // The directory does not expose private credential records or passwords.
}
