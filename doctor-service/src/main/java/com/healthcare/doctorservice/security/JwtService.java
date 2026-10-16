package com.healthcare.doctorservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;       //Secret string(must match auth-service)

    private SecretKey secretKey;        //Signing in key used to validate token

    @PostConstruct      //is used on a method that needs to run automatically exactly once, immediately after the dependency injection is complete and the bean is fully initialized.
    public void init(){
        //Convert secret string into HMAC key (same as auth-service)
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

    }

    // ===============================
    // Extract Username (Email)
    // ===============================

    public String extractUsername(String token){
        return extractAllClaims(token).getSubject();
    }


    // ===============================
    // Extract authUserId (CUSTOM CLAIM)
    // ===============================

    public Long extractAuthUserId(String token){
        Claims claims = extractAllClaims(token);
        Object authUserId = claims.get("authUserId");

        //Handle different number types safely
        if(authUserId instanceof Integer) {
            return ((Integer) authUserId).longValue();
        }

        if(authUserId instanceof Long){
            return (Long) authUserId;
        }

        return Long.parseLong(authUserId.toString());
    }



    // ===============================
    // Extract Role
    // ===============================

    public String extractRole(String token){
        return extractAllClaims(token).get("role", String.class);
    }

    // ===============================
    // Extract Any Claim
    // ===============================
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // ===============================
    // Check Expiration
    // ===============================
    public boolean isTokenExpired(String token){
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }



    // ===============================
    // Extract All Claims
    // ===============================

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
