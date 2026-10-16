package com.healthcare.doctorservice.config;
import com.healthcare.doctorservice.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


/*
 * ============================================================
 *                    SECURITY CONFIGURATION
 * ============================================================
 *
 * Request Flow:
 *
 * Incoming HTTP Request
 *          │
 *          ▼
 * SecurityFilterChain
 *          │
 *          ▼
 * JwtAuthenticationFilter
 *          │
 *          ├── Reads JWT from Authorization header
 *          ├── Validates JWT
 *          ├── Creates CustomUserPrincipal
 *          └── Stores authentication in Spring Security
 *          │
 *          ▼
 * Authorization Rules
 *          │
 *          ├── Public endpoint → Allow request
 *          ├── Authenticated endpoint → Require valid login
 *          └── Role-protected endpoint → Check user role
 *          │
 *          ▼
 * DoctorController
 *
 * ============================================================
 *
 * What is the responsibility of this class?
 *
 * This class defines the security rules for Doctor Service.
 *
 * It tells Spring Security:
 *
 * - Which endpoints are public
 * - Which endpoints require authentication
 * - That the application uses stateless JWT authentication
 * - When JwtAuthenticationFilter should run
 * - That method-level security such as @PreAuthorize is enabled
 *
 * Important:
 *
 * This class does not validate JWT tokens itself.
 * JwtAuthenticationFilter and JwtService perform that work.
 */


@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /*
     * Spring injects our custom JWT filter through constructor injection.
     *
     * Lombok's @RequiredArgsConstructor creates the constructor
     * automatically because this field is final.
     */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * What does this method do?
     *
     * It creates the Spring Security filter chain used for every
     * incoming HTTP request.
     *
     * Why do we need this?
     *
     * Spring Security needs clear instructions about authentication,
     * authorization, sessions, public endpoints, and custom filters.
     *
     * Real-life analogy:
     *
     * Think of this method as the hospital's written security policy.
     *
     * It tells the security team:
     *
     * - Which areas are open to everyone
     * - Which areas require a verified employee badge
     * - Which security guard should inspect the badge
     *
     * Important rule to remember:
     *
     * Authentication asks:
     * "Who are you?"
     *
     * Authorization asks:
     * "What are you allowed to do?"
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http

                /*
                 * Disable CSRF protection.
                 *
                 * CSRF protection is mainly needed when a browser
                 * automatically sends authentication through cookies
                 * or server-side sessions.
                 *
                 * Our application uses JWT tokens sent explicitly in
                 * the Authorization header, so the API is stateless.
                 */
                .csrf(csrf -> csrf.disable())

                /*
                 * Tell Spring Security not to create or use HTTP sessions.
                 *
                 * Every request must include its own JWT token.
                 *
                 * The server does not remember login information between
                 * requests.
                 */
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                /*
                 * Define access rules for API endpoints.
                 *
                 * Swagger and OpenAPI endpoints are public during
                 * development so we can open the documentation and test
                 * the APIs.
                 *
                 * Every other endpoint requires an authenticated user.
                 */
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html"
                        )
                        .permitAll()

                        .anyRequest()
                        .authenticated()
                )

                /*
                 * Add our JWT filter before Spring Security's default
                 * username-password authentication filter.
                 *
                 * This ensures the JWT is read and validated before the
                 * request reaches a protected controller method.
                 */
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        /*
         * Build and return the completed Spring Security configuration.
         */
        return http.build();
    }
}