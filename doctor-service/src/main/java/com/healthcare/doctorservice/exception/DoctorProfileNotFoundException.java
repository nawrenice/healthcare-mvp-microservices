package com.healthcare.doctorservice.exception;



// Thrown when doctor profile does not exist for given authUserId
public class DoctorProfileNotFoundException extends RuntimeException{
    public DoctorProfileNotFoundException(String message){
        super(message);
    }
}
