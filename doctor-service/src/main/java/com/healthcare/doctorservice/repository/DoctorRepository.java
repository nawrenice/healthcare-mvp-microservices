package com.healthcare.doctorservice.repository;
import com.healthcare.doctorservice.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByAuthUserId(Long authUserId);
    Page<Doctor> findByAvailableTrue(Pageable pageable);
}
