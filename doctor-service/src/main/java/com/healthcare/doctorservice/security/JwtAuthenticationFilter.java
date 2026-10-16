package com.healthcare.doctorservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 1. Read authorization header
        String authHeader = request.getHeader("Authorization");
        // Never log bearer tokens: anyone holding one can use the account.

        // 2. If header is missing or does not start with "Bearer", skip this filter and continue the request normally
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Remove "Bearer" prefix and keep only jwt token
        String token = authHeader.substring(7);

        try{
            // 4. Extract values from token using JwtService
            String email = jwtService.extractUsername(token);
            String role = jwtService.extractRole(token);
            Long authUserId = jwtService.extractAuthUserId(token);

            // 5. Check if token is not expired before trusting it
            if(email != null && authUserId != null && authUserId > 0 &&
                    List.of("PATIENT", "DOCTOR", "ADMIN", "RECEPTION").contains(role) && !jwtService.isTokenExpired(token)){
                // 6. Create our custom principal object
                CustomUserPrincipal principal = new CustomUserPrincipal(
                        authUserId,
                        email,
                        role
                );

                //Convert JWT role into spring security authority
                //Example: PATIENT -> ROLE_PATIENT
                SimpleGrantedAuthority authority =
                        new SimpleGrantedAuthority("ROLE_" +role);

                // 7. Create Spring security authentication object with principal + authorities
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(authority)
                );

                // 8. Add request details
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 9. Save authenticated user into security context
                SecurityContextHolder.getContext().setAuthentication(authentication);

            }
        }catch (Exception ex){
            // 10. If token is invalid/malformed/expired, clear context
            SecurityContextHolder.clearContext();
        }

        //11. Continue filter chain
        filterChain.doFilter(request,response);

    }
}
