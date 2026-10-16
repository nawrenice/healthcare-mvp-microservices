package com.healthcare.authservice.service;


import com.healthcare.authservice.entity.User;
import com.healthcare.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService{

    private final UserRepository userRepository;

    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException{
        //Normalize email so lookup stays consistent with signup/login
        String normalizedEmail = email.trim().toLowerCase(java.util.Locale.ROOT);

        //load user from database
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() ->
        new UsernameNotFoundException("User not found with email " + normalizedEmail));

        //convert your user entity into spring's built-in UserDetails object
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }

}



