package com.healthcare.doctorservice.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public class ApiResponse {

    private boolean success;

    private String message;

    private int status;

    private LocalDateTime timeStamp;
}
