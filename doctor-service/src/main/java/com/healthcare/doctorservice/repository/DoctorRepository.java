package com.healthcare.doctorservice.repository;

import com.healthcare.doctorservice.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    //Find doctor by auth-service user id;
    Optional<Doctor> findByAuthUserId(Long authUserId);

    //used to prevent duplicate doctor profiles
    boolean existsByAuthUserId(Long authUserId);

    // find doctors whose available flag is true.
    List<Doctor> findByAvailableTrue();
}
