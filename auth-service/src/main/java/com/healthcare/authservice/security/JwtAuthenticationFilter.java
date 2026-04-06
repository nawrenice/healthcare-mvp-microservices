package com.healthcare.authservice.security;


import com.healthcare.authservice.service.CustomUserDetailsService;
import com.healthcare.authservice.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        /*1. Get authorization header
         Read Header: example: Authorization: Bearer eyJhbGciOiJIUzI1Ni... */
        final String authHeader = request.getHeader("Authorization");

        String jwt = null;
        String userEmail = null;

        /*2. Check if header exists and starts with "Bearer"
          If no token -> skip authentication        */
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            //No JWT -> continue request without authentication
            filterChain.doFilter(request,response);
            return;
        }

        /*3. Extract token(remove "bearer ")
        Removes "Bearer " → keeps only token */
        jwt = authHeader.substring(7);

        try{
            //4. Extract email from token
            userEmail = jwtService.extractUsername(jwt);

            //5. If email exists and no authentication yet
            if(userEmail!= null && SecurityContextHolder.getContext().getAuthentication()== null){

                //6. Load user from database
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(userEmail);

                //7. Validate token
                if(jwtService.isTokenValid(jwt,userDetails)){

                    //8. Create authentication object
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    //9. Attach request details(IP, session, etc)
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    //10. Set authentication in Spring context
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }catch (Exception e){
            //Token invalid / expired / malformed -> ignore and continue
            //Later we can add logging here
        }

        //11. Continue filter chain
        filterChain.doFilter(request, response);

    }
}
