package com.healthcare.doctorservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/*
 * ============================================================
 *                 JWT AUTHENTICATION FILTER
 * ============================================================
 *
 * Request Flow:
 *
 * Client sends request with JWT token
 *        │
 *        ▼
 * JwtAuthenticationFilter  ← This class
 *        │
 *        ├── Reads Authorization header
 *        ├── Extracts token
 *        ├── Validates token using JwtService
 *        ├── Creates CustomUserPrincipal
 *        ├── Stores user in Spring Security
 *        ▼
 * DoctorController
 *
 * Purpose:
 * This filter runs before controller methods and prepares the
 * authenticated user for the current request.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    /*
     * Runs once for every incoming HTTP request.
     *
     * Why do we need this?
     * Before a protected controller endpoint is reached, we need
     * to check whether the request has a valid JWT token.
     *
     * Important:
     * Controllers should not parse JWT tokens directly.
     * This filter handles authentication before the request reaches
     * the controller.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        /*
         * If the request does not contain a Bearer token,
         * we do not authenticate the user here.
         *
         * The request continues, and Spring Security will decide
         * whether the endpoint is public or protected.
         */
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        /*
         * Remove "Bearer " from the header so only the JWT token remains.
         *
         * Example:
         * Authorization: Bearer abc.def.ghi
         *
         * token = abc.def.ghi
         */
        String token = authHeader.substring(7);

        /*
         * Validate the token before trusting any information inside it.
         *
         * If the token is expired, modified, or signed with the wrong secret,
         * we do not create an authenticated user.
         */
        if (!jwtService.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        Long authUserId = jwtService.extractAuthUserId(token);
        String email = jwtService.extractEmail(token);
        String role = jwtService.extractRole(token);

        /*
         * Create our application's logged-in user object.
         *
         * This is the "badge" created after the JWT has been verified.
         */
        CustomUserPrincipal principal =
                new CustomUserPrincipal(authUserId, email, role);

        /*
         * Convert the user's role into a Spring Security authority.
         *
         * Spring Security expects roles in the format ROLE_DOCTOR,
         * ROLE_PATIENT, ROLE_ADMIN, etc.
         */
        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority("ROLE_" + role);

        /*
         * Create an authentication object for Spring Security.
         *
         * principal  = logged-in user information
         * credentials = null because password is not needed after JWT validation
         * authorities = user's role/permissions
         */
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(authority)
                );

        /*
         * Store the authenticated user in Spring Security for this request.
         *
         * After this, controller methods can access the user using:
         *
         * @AuthenticationPrincipal CustomUserPrincipal principal
         */
        SecurityContextHolder.getContext().setAuthentication(authentication);

        /*
         * Continue the request flow so it can reach the controller.
         */
        filterChain.doFilter(request, response);
    }
}