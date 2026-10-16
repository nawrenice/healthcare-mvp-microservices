package com.healthcare.appointmentservice.service;

import com.healthcare.appointmentservice.client.DoctorDirectoryClient;
import com.healthcare.appointmentservice.dto.request.BookAppointmentRequest;
import com.healthcare.appointmentservice.dto.response.AppointmentResponse;
import com.healthcare.appointmentservice.entity.*;
import com.healthcare.appointmentservice.repository.AppointmentRepository;
import com.healthcare.appointmentservice.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import static org.springframework.http.HttpStatus.*;

@Service @RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppointmentService {
    private final AppointmentRepository appointments;
    private final DoctorDirectoryClient doctorDirectory;

    /* BOOK: fixed 30-minute UTC slots make overlap prevention enforceable with database uniqueness.
     * Checking "does it exist?" before INSERT alone is unsafe: two requests could both see an empty slot.
     */
    @Transactional
    public AppointmentResponse book(Long patientId, BookAppointmentRequest request, String authorization) {
        Instant start = request.getStartsAt();
        if (!start.isAfter(Instant.now()) || start.getEpochSecond() % 1800 != 0 || start.getNano() != 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Choose a future 30-minute slot (HH:00 or HH:30 UTC)");
        }
        var doctor = doctorDirectory.getDoctor(request.getDoctorId(), authorization);
        if (!doctor.available()) {
            throw new ResponseStatusException(CONFLICT, "Doctor is not accepting appointments");
        }
        Appointment appointment = Appointment.builder()
                .patientAuthUserId(patientId).doctorAuthUserId(doctor.authUserId()).doctorId(request.getDoctorId())
                .startsAt(start).endsAt(start.plus(30, ChronoUnit.MINUTES)).reason(request.getReason().trim())
                .status(AppointmentStatus.BOOKED)
                .doctorSlotKey(doctor.authUserId() + ":" + start)
                .patientSlotKey(patientId + ":" + start).build();
        // Flush inside the request so a uniqueness violation becomes a clear HTTP 409.
        return mapToResponse(appointments.saveAndFlush(appointment));
    }

    public Page<AppointmentResponse> mine(CustomUserPrincipal user, int page, int size) {
        var paging = PageRequest.of(Math.max(0, page), Math.max(1, Math.min(size, 100)), Sort.by("startsAt").descending());
        return (user.getRole().equals("DOCTOR")
                ? appointments.findByDoctorAuthUserId(user.getAuthUserId(), paging)
                : appointments.findByPatientAuthUserId(user.getAuthUserId(), paging)).map(this::mapToResponse);
    }

    public AppointmentResponse get(Long id, CustomUserPrincipal user) {
        Appointment appointment = find(id);
        requireParticipant(appointment, user);
        return mapToResponse(appointment);
    }

    @Transactional
    public AppointmentResponse cancel(Long id, CustomUserPrincipal user) {
        Appointment appointment = find(id);
        requireParticipant(appointment, user);
        if (appointment.getStatus() != AppointmentStatus.BOOKED || !appointment.getStartsAt().isAfter(Instant.now())) {
            throw new ResponseStatusException(CONFLICT, "Only future booked appointments can be cancelled");
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setDoctorSlotKey(null);
        appointment.setPatientSlotKey(null);
        return mapToResponse(appointments.saveAndFlush(appointment));
    }

    @Transactional
    public AppointmentResponse complete(Long id, CustomUserPrincipal user) {
        Appointment appointment = find(id);
        if (!user.getRole().equals("DOCTOR") || !appointment.getDoctorAuthUserId().equals(user.getAuthUserId())) {
            throw new ResponseStatusException(FORBIDDEN, "Only the assigned doctor can complete this appointment");
        }
        if (appointment.getStatus() != AppointmentStatus.BOOKED || appointment.getEndsAt().isAfter(Instant.now())) {
            throw new ResponseStatusException(CONFLICT, "Only a booked appointment that has ended can be completed");
        }
        appointment.setStatus(AppointmentStatus.COMPLETED);
        return mapToResponse(appointments.saveAndFlush(appointment));
    }

    private Appointment find(Long id) {
        return appointments.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Appointment not found"));
    }

    private void requireParticipant(Appointment appointment, CustomUserPrincipal user) {
        boolean ownsPatient = user.getRole().equals("PATIENT") && appointment.getPatientAuthUserId().equals(user.getAuthUserId());
        boolean ownsDoctor = user.getRole().equals("DOCTOR") && appointment.getDoctorAuthUserId().equals(user.getAuthUserId());
        if (!ownsPatient && !ownsDoctor) {
            throw new ResponseStatusException(FORBIDDEN, "This appointment belongs to another user");
        }
    }

    private AppointmentResponse mapToResponse(Appointment a) {
        return AppointmentResponse.builder().id(a.getId()).patientAuthUserId(a.getPatientAuthUserId())
                .doctorId(a.getDoctorId()).doctorAuthUserId(a.getDoctorAuthUserId())
                .startsAt(a.getStartsAt()).endsAt(a.getEndsAt()).reason(a.getReason())
                .status(a.getStatus()).createdAt(a.getCreatedAt()).build();
    }
}
