package com.healthcare.doctorservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity             // Marks this class as a JPA entity (mapped to a database table)
@Table(name ="doctors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder                // Lombok Builder pattern
                        // Allows flexible and readable object creation without needing multiple constructors
public class Doctor {

    //local primary key for doctor-service database
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)     //primary-key auto increment
    private Long id;


    // Comes from auth-service JWT.
    // This links doctor-service record to the authenticated user.
    @Column(nullable = false, unique = true)
    private Long authUserId;

    @Column(nullable = false)
    private String specialization;

    @Column(nullable = false)
    private Integer yearsOfExperience;

    @Column(nullable = false)
    private String qualification;

    private BigDecimal consultationFee;

    private Boolean available;

    private String hospitalName;

    private String department;

    //created timestamp
    private LocalDateTime createdAt;

    //updated timestamp
    private LocalDateTime updatedAt;

    // This method runs automatically BEFORE the entity is inserted into the database
    @PrePersist
    protected void onCreate() {

        // Set creation timestamp when the record is first saved
        createdAt = LocalDateTime.now();

        // Also initialize updatedAt at creation time
        updatedAt = LocalDateTime.now();

        // If 'available' was not explicitly set, default it to true
        // This ensures new doctors are available by default
        if (available == null) {
            available = true;
        }
    }

    // This method runs automatically BEFORE the entity is updated in the database
    @PreUpdate
    protected void onUpdate() {

        // Update the timestamp whenever the record is modified
        updatedAt = LocalDateTime.now();
    }



}
