package com.healthcare.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration          //mark this as config class
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder(){
        // BCrypt is a strong one-way hashing algorithm for passwords
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                //Disable CSRF now because this is a stateless rest API tested via postman/frontend
                .csrf(csrf -> csrf.disable())

                //Define which endpoints are public and which are required authentication
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()   //Signup and Login must be public
                        .anyRequest().authenticated()
                )

                //Temporary only; later JWT will replace this
                .httpBasic(Customizer.withDefaults());
        return http.build();

    }
}
