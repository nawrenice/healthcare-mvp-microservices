package com.healthcare.doctorservice.dto.response;

import lombok.*;

import java.time.LocalDateTime;

/*
 * Standard API response wrapper.
 *
 * Enterprise applications commonly use a consistent response format
 * so frontend developers always know what structure to expect.
 *
 * T means this response can hold any type of data:
 * DoctorProfileResponse, List<DoctorProfileResponse>, String, etc.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    private boolean success;

    private String message;

    private int status;

    private LocalDateTime timeStamp;

    //Generic response body
    // Example: DoctorProfileResponse, list of doctors , or null
    private T data;
}
