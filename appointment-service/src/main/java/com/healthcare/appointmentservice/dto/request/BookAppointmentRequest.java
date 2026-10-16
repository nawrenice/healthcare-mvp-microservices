package com.healthcare.appointmentservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter @Setter
public class BookAppointmentRequest {
    @NotNull @Positive private Long doctorId;
    @NotNull @Future private Instant startsAt;
    @NotBlank @Size(max = 500) private String reason;
    // There is deliberately no patient ID: it always comes from the authenticated principal.
}
