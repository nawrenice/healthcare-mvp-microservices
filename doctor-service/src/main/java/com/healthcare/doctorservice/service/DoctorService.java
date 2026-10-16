package com.healthcare.doctorservice.service;


import com.healthcare.doctorservice.dto.request.DoctorProfileCreateRequest;
import com.healthcare.doctorservice.dto.request.DoctorProfileUpdateRequest;
import com.healthcare.doctorservice.dto.response.DoctorProfileResponse;
import com.healthcare.doctorservice.entity.Doctor;
import com.healthcare.doctorservice.exception.DoctorProfileNotFoundException;
import com.healthcare.doctorservice.exception.DuplicateDoctorProfileException;
import com.healthcare.doctorservice.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;


         /*########  CREATE DOCTOR PROFILE  ###########
         * Creates a doctor profile for the authenticated user.

         * Business Rule:
            * One auth user can have only one doctor profile.

         * Example:
            * authUserId = 101
            * -> Doctor Profile Created

         * If profile already exists:
         * -> Throw exception
         */

    public DoctorProfileResponse createDoctorProfile(Long authUserId,
                                                     DoctorProfileCreateRequest request){
        //prevent duplicate doctor profiles for the same user
        if (doctorRepository.existsByAuthUserId(authUserId)) {
            throw new DuplicateDoctorProfileException(
                 "Doctor profile already exist for user id: " + authUserId
            );
        }

        //Convert the request DTO into Doctor entity
        //DTO = data coming from API request
        // Entity = object saved into database.

        Doctor doctor = Doctor.builder()
                .authUserId(authUserId)
                .specialization(request.getSpecialization())
                .yearsOfExperience(request.getYearsOfExperience())
                .qualification(request.getQualification())
                .consultationFee(request.getConsultationFee())
                .available(request.getAvailable() != null ? request.getAvailable() : true)
                .hospitalName(request.getHospitalName())
                .department(request.getDepartment())
                .build();

        // Save doctor entity into doctors table.
        Doctor savedDoctor = doctorRepository.save(doctor);

        // Convert saved entity into response DTO.
        // We do not return entity directly because response DTO gives us control
        // over what data is exposed to frontend.
        return mapToResponse(savedDoctor);
    }

    /*  ########  GET DOCTOR PROFILE  ###########
     * Returns the doctor profile for the authenticated user.
     *
     * Business Purpose:
     * After login, a doctor should be able to view
     * their own profile information.
     *
     * Example:
     * authUserId = 101
     * -> returns doctor's profile
     */
    public DoctorProfileResponse getDoctorProfile(Long authUserId){
        Doctor doctor = doctorRepository.findByAuthUserId(authUserId)
                .orElseThrow(() ->
                        new DoctorProfileNotFoundException(
                                "Doctor profile not found for user id: " + authUserId
                        ));
        //convert entity into response DTO.
        return mapToResponse(doctor);
    }

    /* ########  UPDATE DOCTOR PROFILE  ###########
     * Updates an existing doctor profile.
     *
     * Business Purpose:
     * Allows a doctor to modify profile information such as
     * specialization, qualification, consultation fee,
     * hospital details, and availability.
     */
    public DoctorProfileResponse updateDoctorProfile(Long authUserId,
                                                     DoctorProfileUpdateRequest request){

        //Retrieve existing profile of a doctor
        //If profile does not exist, stop processing
        Doctor doctor = doctorRepository.findByAuthUserId(authUserId)
                .orElseThrow(()-> new DoctorProfileNotFoundException(
                        "Doctor profile not found with user id : "+ authUserId
                ));

        //Update profile with new values provided in request.
        doctor.setSpecialization(request.getSpecialization());
        doctor.setYearsOfExperience(request.getYearsOfExperience());
        doctor.setQualification(request.getQualification());
        doctor.setConsultationFee(request.getConsultationFee());
        doctor.setAvailable(request.getAvailable());
        doctor.setHospitalName(request.getHospitalName());
        doctor.setDepartment(request.getDepartment());

        //Persist updated profile in the database
        Doctor updatedDoctor = doctorRepository.save(doctor);

        //Convert entity into response DTO
        return mapToResponse(updatedDoctor);
    }

   /* ########  GET All DOCTORS  ###########
    * Returns all doctor profiles in the system.
    *
    * Business Purpose:
    * Allows patients, receptionists, or admins
    * to view available doctors.

    */
    public List<DoctorProfileResponse> getAllDoctors(){

        //fetch all doctors records from the db
        List<Doctor> doctorList = doctorRepository.findAll();

        //Convert each doctor entity into DoctorProfileResponse DTO
        return doctorList.stream()
                .map(this::mapToResponse) // this can also be written as .map(doctors ->mapToResponse(doctors))
                .toList();
    }

    /* ########  GET All AVAILABLE DOCTORS  ###########
     * Returns all available doctor profiles in the system.
     *
     * Business Purpose:
     * Allows patients, receptionists, or admins
     * to view available doctors.

     */

    public List<DoctorProfileResponse> getAvailableDoctors(){
        List<Doctor> doctors = doctorRepository.findByAvailableTrue();

        return doctors.stream()
                //Only keep doctors whose available flag is true.
             //   .filter(Doctor::getAvailable) is not required because findByAvailableTrue() will do this part
                .map(this::mapToResponse)
                .toList();

    }


    /* ########  MAP DOCTOR ENTITY INTO DTO  ###########
     * Converts Doctor entity into DoctorProfileResponse DTO.
     *
     * We use this helper method to avoid repeating the same mapping code
     * in create, update, get, and list methods.
     */
    private DoctorProfileResponse mapToResponse(Doctor doctor) {

        return DoctorProfileResponse.builder()
                .id(doctor.getId())
                .authUserId(doctor.getAuthUserId())
                .specialization(doctor.getSpecialization())
                .yearsOfExperience(doctor.getYearsOfExperience())
                .qualification(doctor.getQualification())
                .consultationFee(doctor.getConsultationFee())
                .available(doctor.getAvailable())
                .hospitalName(doctor.getHospitalName())
                .department(doctor.getDepartment())
                .createdAt(doctor.getCreatedAt())
                .updatedAt(doctor.getUpdatedAt())
                .build();
    }
}
