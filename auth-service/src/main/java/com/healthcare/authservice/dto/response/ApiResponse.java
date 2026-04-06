package com.healthcare.authservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse {
    private boolean success; //tells client whether operation succeeded
    private String message; //Human-readable response message
    private int status;
}
