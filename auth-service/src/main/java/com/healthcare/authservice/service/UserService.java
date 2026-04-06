package com.healthcare.authservice.service;

import com.healthcare.authservice.dto.response.ApiResponse;
import com.healthcare.authservice.dto.request.UserSignupRequest;
import com.healthcare.authservice.entity.User;
import com.healthcare.authservice.exception.UserAlreadyExistsException;
import com.healthcare.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


/**
 * Service layer handles business logic.
 * Keeps controller thin.
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /*
    // Constructor injection (preferred for testability) - Because  ->
    “Instead of creating dependencies manually, we use constructor injection
    so Spring can manage them. This improves loose coupling, testability, and
    maintainability.”
    */
//    public UserService(UserRepository userRepository,         //||   \\           NOTE:
//                       PasswordEncoder passwordEncoder) {     //||====\\   this 4 line of codes can be replaced with
//        this.userRepository = userRepository;                 //||====//   @RequiredArgsConstructor.
//        this.passwordEncoder = passwordEncoder;               //||   //
//    }

    /**
     * Handles user signup
     */
    public ApiResponse signup(UserSignupRequest signupRequest) {
        // Normalize email to avoid duplicate due to case or spaces
        String normalizedEmail = signupRequest.getEmail().trim().toLowerCase();
        // 1. Check if email already exists
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new UserAlreadyExistsException(
                    "User with email " + normalizedEmail + " already exists!!!"
            );
        }

        // 2. Map DTO -> Entity
        User user = new User();
        user.setName(signupRequest.getName()); //save user's name
        user.setEmail(normalizedEmail); //save user's unique email

        // Hash password before saving
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));  //hash password

        // Direct enum mapping
        user.setRole(signupRequest.getRole());  //save selected role

        // 3. Save user
        userRepository.save(user);      //saves user in db

        return new ApiResponse(
                true,
                "User registered successfully",
                201
        );

    }
}
