package com.healthcare.doctorservice.exception;



// Thrown when doctor tries to create profile more than once
public class DuplicateDoctorProfileException extends RuntimeException {

    public DuplicateDoctorProfileException(String message){
        super(message);
    }
}
