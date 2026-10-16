package com.healthcare.appointmentservice.dto.response;

import com.healthcare.appointmentservice.entity.AppointmentStatus;
import lombok.Builder;
import lombok.Getter;
import java.time.Instant;

@Getter @Builder
public class AppointmentResponse {
    private Long id;
    private Long patientAuthUserId;
    private Long doctorAuthUserId;
    private Long doctorId;
    private Instant startsAt;
    private Instant endsAt;
    private String reason;
    private AppointmentStatus status;
    private Instant createdAt;
}
