package com.healthcare.doctorservice.service;

import com.healthcare.doctorservice.dto.request.DoctorProfileRequest;
import com.healthcare.doctorservice.dto.response.DoctorProfileResponse;
import com.healthcare.doctorservice.entity.Doctor;
import com.healthcare.doctorservice.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;

@Service @RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorService {
    private final DoctorRepository doctors;

    /* CREATE: take ownership from the verified token, so a caller cannot create another doctor's profile. */
    @Transactional
    public DoctorProfileResponse create(Long authUserId, DoctorProfileRequest request) {
        if (doctors.findByAuthUserId(authUserId).isPresent()) {
            throw new ResponseStatusException(CONFLICT, "Doctor profile already exists");
        }
        Doctor doctor = Doctor.builder().authUserId(authUserId).build();
        applyChanges(doctor, request);
        return mapToOwnResponse(doctors.save(doctor));
    }

    public DoctorProfileResponse get(Long id) {
        return mapToResponse(doctors.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Doctor not found")));
    }

    public DoctorProfileResponse getMine(Long authUserId) {
        return mapToOwnResponse(findMine(authUserId));
    }

    @Transactional
    public DoctorProfileResponse update(Long authUserId, DoctorProfileRequest request) {
        Doctor doctor = findMine(authUserId);
        applyChanges(doctor, request);
        return mapToOwnResponse(doctors.save(doctor));
    }

    /* Let SQL filter the directory; pagination bounds the amount of data loaded per request. */
    public Page<DoctorProfileResponse> list(boolean availableOnly, int page, int size) {
        var paging = PageRequest.of(Math.max(0, page), Math.max(1, Math.min(size, 100)), Sort.by("id"));
        return (availableOnly ? doctors.findByAvailableTrue(paging) : doctors.findAll(paging))
                .map(this::mapToResponse);
    }

    private Doctor findMine(Long authUserId) {
        return doctors.findByAuthUserId(authUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Doctor profile not found"));
    }

    private void applyChanges(Doctor doctor, DoctorProfileRequest request) {
        doctor.setName(request.getName().trim());
        doctor.setSpecialty(request.getSpecialty().trim());
        doctor.setLicenseNumber(request.getLicenseNumber().trim());
        doctor.setClinicAddress(request.getClinicAddress().trim());
        doctor.setAvailable(request.isAvailable());
    }

    private DoctorProfileResponse mapToOwnResponse(Doctor doctor) {
        return mapToResponse(doctor).toBuilder().licenseNumber(doctor.getLicenseNumber()).build();
    }

    private DoctorProfileResponse mapToResponse(Doctor doctor) {
        return DoctorProfileResponse.builder().id(doctor.getId()).authUserId(doctor.getAuthUserId())
                .name(doctor.getName()).specialty(doctor.getSpecialty())
                .clinicAddress(doctor.getClinicAddress()).available(doctor.isAvailable()).build();
    }
}
