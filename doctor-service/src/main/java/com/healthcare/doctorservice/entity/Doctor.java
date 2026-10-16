package com.healthcare.doctorservice.entity;

import jakarta.persistence.*;
import lombok.*;

/** A doctor's public directory information. Credentials and passwords belong to auth-service. */
@Entity
@Table(name = "doctors")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Doctor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // A logical cross-service link, not a JPA relationship to another service's database.
    @Column(nullable = false, unique = true)
    private Long authUserId;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(nullable = false, length = 120)
    private String specialty;
    @Column(nullable = false, unique = true, length = 80)
    private String licenseNumber;
    @Column(nullable = false, length = 200)
    private String clinicAddress;
    @Column(nullable = false)
    private boolean available;
}
