package com.healthcare.appointmentservice.repository;
import com.healthcare.appointmentservice.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    Page<Appointment> findByPatientAuthUserId(Long id, Pageable pageable);
    Page<Appointment> findByDoctorAuthUserId(Long id, Pageable pageable);
}
