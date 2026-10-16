package com.healthcare.appointmentservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name = "appointments")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Appointment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private Long patientAuthUserId;
    @Column(nullable = false) private Long doctorAuthUserId;
    @Column(nullable = false) private Long doctorId;
    @Column(nullable = false) private Instant startsAt;
    @Column(nullable = false) private Instant endsAt;
    @Column(nullable = false, length = 500) private String reason;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private AppointmentStatus status;
    @Column(nullable = false, updatable = false) private Instant createdAt;
    // The DB arbitrates simultaneous requests, even across multiple service instances.
    // Cancellation clears these keys so the slot may be booked again; history remains intact.
    @Column(unique = true) private String doctorSlotKey;
    @Column(unique = true) private String patientSlotKey;
    // Reject a stale cancellation/completion rather than silently overwriting another update.
    @Version private Long version;
    @PrePersist void onCreate() { createdAt = Instant.now(); }
}
