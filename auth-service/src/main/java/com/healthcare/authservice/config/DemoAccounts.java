package com.healthcare.authservice.config;

import com.healthcare.authservice.entity.Role;
import com.healthcare.authservice.entity.User;
import com.healthcare.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates a synthetic doctor only under the explicit demo profile. Never enable this profile in production. */
@Component
@Profile("demo")
@RequiredArgsConstructor
public class DemoAccounts implements CommandLineRunner {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        if (users.findByEmail("doctor@example.test").isEmpty()) {
            users.save(User.builder().name("Demo Doctor").email("doctor@example.test")
                    .password(encoder.encode("DemoDoctor123!" )).role(Role.DOCTOR).build());
        }
    }
}
