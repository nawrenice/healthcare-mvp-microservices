package com.healthcare.authservice.dto.response;

import com.healthcare.authservice.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class AuthResponse {

    private boolean success; //Indicates successful authentication
    private String message;  //Message for the client
    private String token;    //JWT token returned after successful login
    private String email;   //Useful for front-end display or debugging
    private Role role;      //Role returned so that client knows the authenticated user type
}
