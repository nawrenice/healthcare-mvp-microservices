package com.healthcare.authservice.service;

/*
* Handles authentication logic (login, JWT)
* */

import com.healthcare.authservice.dto.response.AuthResponse;
import com.healthcare.authservice.dto.request.UserLoginRequest;
import com.healthcare.authservice.entity.User;
import com.healthcare.authservice.exception.InvalidCredentialsException;
import com.healthcare.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;        //Handles DB access for User records
    private final PasswordEncoder passwordEncoder; //Used for hashing and password verification
    private final JwtService jwtService; //Responsible for JWT generation

    /* Handles User Login */

    public AuthResponse userLogin(UserLoginRequest loginRequest){

        // Normalized email for lower case
        String normalizedEmail = loginRequest.getEmail().trim().toLowerCase();

        // 1. Fetch user by email
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        //2 . Compare password (raw vs hashed)  syntax: passwordEncoder.matches(raw, hashed)
        boolean isMatch = passwordEncoder.matches(
                loginRequest.getPassword(),     //raw password from request
                user.getPassword()              // hashed pw from DB
        );

        // 3. If password is incorrect -> Reject
        if(!isMatch){
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);
        // 4. Login success
        return  AuthResponse.builder()
                .success(true)
                .message("Login Successful")
                .token(token)
                .email(user.getEmail())
                .role(user.getRole())
                .build();

    }
}
