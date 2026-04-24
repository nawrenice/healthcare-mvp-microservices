package com.healthcare.doctorservice.exception;



import com.healthcare.doctorservice.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;


// @RestControllerAdvice makes this class a global exception handler.
// It listens for exceptions thrown from controllers/services and sends clean JSON error responses.
@RestControllerAdvice
public class GlobalExceptionHandler {



    // Handles DoctorProfileNotFoundException anywhere in the application.
    // Example: when a doctor profile does not exist in the database.
    @ExceptionHandler(DoctorProfileNotFoundException.class)
    public ResponseEntity<ApiResponse> handleNotFound(DoctorProfileNotFoundException ex){

        // Build a clean and consistent error response body.
        ApiResponse response = ApiResponse.builder()
                .success(false) // false because this is an error response
                .message(ex.getMessage()) // message from the exception we threw
                .status(HttpStatus.NOT_FOUND.value()) // 404 as an integer
                .timeStamp(LocalDateTime.now()) // time when error happened
                .build();

        // ResponseEntity controls BOTH:
        // 1. response body: the JSON sent to client
        // 2. HTTP status code: 404 NOT FOUND
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // Handles DuplicateDoctorProfileException.
    // Example: when a doctor tries to create a profile twice.
    @ExceptionHandler(DuplicateDoctorProfileException.class)
    public ResponseEntity<ApiResponse> handleDuplicateDoctorProfile(
            DuplicateDoctorProfileException ex
    ) {

        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .status(HttpStatus.BAD_REQUEST.value()) // 400
                .timeStamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Handles validation errors from @Valid.
    // Example: specialization is blank, yearsOfExperience is negative, etc.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationException(
            MethodArgumentNotValidException ex
    ) {

        // Get the first validation error message from the DTO.
        // Example: "Specialization is required"
        String errorMessage = ex.getBindingResult()
                .getFieldError()
                .getDefaultMessage();

        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message(errorMessage)
                .status(HttpStatus.BAD_REQUEST.value()) // 400
                .timeStamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Fallback handler for unexpected errors.
    // This prevents raw Java errors/stack traces from being exposed to the client.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGenericException(Exception ex) {

        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message("Something went wrong")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value()) // 500
                .timeStamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }




}





/*
* @RestControllerAdvice:
Global error catcher for REST APIs.
@ExceptionHandler(SomeException.class):
Tells Spring: “If this exception happens, run this method.”
HttpStatus.NOT_FOUND.value()
Gives integer value 404.
HttpStatus.NOT_FOUND
Sets actual HTTP status code to 404.
ex.getMessage()
Gets the message from the exception we threw.
LocalDateTime.now()
Adds the current server time to the response.
@ExceptionHandler(Exception.class)
Catch-all safety net for unexpected errors.
*/
