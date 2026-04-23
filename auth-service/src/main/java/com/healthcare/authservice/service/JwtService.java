package com.healthcare.authservice.service;

import com.healthcare.authservice.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {
    @Value("${jwt.secret}")
    private String secret; //secret string from application properties

    @Value("${jwt.expiration}")
    private long jwtExpiration;  //toke validity in milliseconds

    private SecretKey secretKey;   //actual signing key used by JJWT

    @PostConstruct
    public void init(){
        //Convert configured secret string into a secure HMAC signing key
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

    }

    // ===============================
    // Generate JWT Token
    // ===============================

    public String generateToken(User user){
        Date now = new Date(); //current time
        Date expiryDate = new Date(now.getTime() + jwtExpiration); //Expiration time

        return Jwts.builder()
                .subject(user.getEmail())   //main identity inside token
                .claim("role", user.getRole().name())   //Add role for authorization use later
                .claim("authUserId", user.getId())
                .issuedAt(now)      //Token issued time
                .expiration(expiryDate) //Token expiry time
                .signWith(secretKey)        //Sign token with secret key
                .compact();                 //build final JWT string
    }


    // ===============================
    // Extract Username (Email)
    // ===============================
    public String extractUsername(String token){
        return extractClaim(token, Claims::getSubject);
    }

    // ===============================
    // Extract Any Claim
    // ===============================
    public <T> T  extractClaim(String token, Function<Claims, T> claimsResolver){
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }



    // ===============================
    // Validate Token
    // ===============================
    public boolean isTokenValid(String token, UserDetails userDetails){
        String email = extractUsername(token);

        return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // ===============================
    // Check Expiration
    // ===============================
    private boolean isTokenExpired(String token) {
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
