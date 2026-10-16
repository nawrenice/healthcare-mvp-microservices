package com.healthcare.doctorservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;



/*
 * ============================================================
 *                     JWT SERVICE
 * ============================================================
 *
 * Authentication Flow
 * -------------------
 *
 * Doctor logs into the application
 *            │
 *            ▼
 *       Auth Service
 *            │
 *            │ Generates JWT containing:
 *            │  - email
 *            │  - role
 *            │  - authUserId
 *            ▼
 *       Doctor Service
 *            │
 *            ▼
 *      JwtAuthenticationFilter
 *            │
 *            ▼
 *        JwtService (This Class)
 *            │
 *            ├── Validate JWT signature
 *            ├── Read all claims
 *            ├── Extract email
 *            ├── Extract role
 *            └── Extract authUserId
 *            │
 *            ▼
 *     CustomUserPrincipal
 *            │
 *            ▼
 *     DoctorController
 *
 * ============================================================
 *
 * Class Responsibility:
 * ---------------------
 * This class is responsible for validating JWT tokens created
 * by Auth Service and extracting authenticated user information.
 *
 * Important:
 * This class NEVER creates JWT tokens.
 * Token generation belongs only to Auth Service.
 *
 * Doctor Service only trusts and reads existing tokens.
 *
 * ============================================================
 */



@Service
public class JwtService {


    /*
     * Secret key used to validate JWT signatures.
     *
     * The value is loaded from application.properties.
     *
     * Important:
     * This secret MUST be identical to the secret used
     * by Auth Service when generating JWT tokens.
     */
    @Value("${jwt.secret}")
    private String secret;


    /*
     * Reusable SecretKey object used to verify JWT signatures.
     *
     * Instead of creating a new SecretKey for every request,
     * we create it once during application startup.
     */
    private SecretKey secretKey;

    /*
     * What does this method do?
     * -------------------------
     * Initializes the SecretKey used to validate JWT tokens.
     *
     * Why do we need this?
     * --------------------
     * The JWT library requires a SecretKey object instead of
     * a plain String. We convert the configured secret once
     * when the application starts.
     *
     * Real-life analogy:
     * ------------------
     * Imagine opening a hospital every morning.
     * Before seeing patients, you unlock the building once.
     * You don't unlock the building every time a patient enters.
     *
     * @PostConstruct means:
     * ---------------------
     * Run this method automatically once after Spring creates
     * this object and injects all required properties.
     *
     * Important rule:
     * ---------------
     * Doctor Service and Auth Service must use the same
     * jwt.secret value.
     */
    @PostConstruct
    public void init() {

        // Convert the configured JWT secret into a reusable SecretKey.
        this.secretKey =
                Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }



    /*
     * What does this method do?
     * -------------------------
     * Reads all information stored inside the JWT.
     *
     * Why do we need this?
     * --------------------
     * Every JWT contains useful information called "claims",
     * such as:
     *
     * - email
     * - role
     * - authUserId
     * - issue time
     * - expiration time
     *
     * Instead of parsing the JWT multiple times, we parse it
     * once here and allow other methods to reuse the result.
     *
     * Real-life analogy:
     * ------------------
     * Think of opening an envelope once and reading everything
     * inside, instead of opening the same envelope repeatedly.
     *
     * Important rule:
     * ---------------
     * If the JWT has been modified, signed using a different
     * secret, or is invalid, parsing will fail.
     */
    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    /*
     * What does this method do?
     * -------------------------
     * Retrieves the authenticated user's email from the JWT.
     *
     * Why do we need this?
     * --------------------
     * Some business operations require knowing which user
     * made the request.
     *
     * Important rule:
     * ---------------
     * The email is stored as the JWT subject by Auth Service.
     */
    public String extractEmail(String token) {

        return extractAllClaims(token).getSubject();
    }



    /*
     * What does this method do?
     * -------------------------
     * Retrieves the authenticated user's unique ID.
     *
     * Why do we need this?
     * --------------------
     * Doctor Service uses authUserId to identify which
     * authenticated account owns the doctor profile.
     *
     * Real-life analogy:
     * ------------------
     * Think of authUserId as the government-issued ID number
     * printed on your identity card.
     *
     * Important rule:
     * ---------------
     * authUserId always comes from the JWT.
     * Never trust the client to send this value.
     */
    public Long extractAuthUserId(String token) {

        return extractAllClaims(token)
                .get("authUserId", Long.class);
    }


    /*
     * What does this method do?
     * -------------------------
     * Retrieves the authenticated user's role.
     *
     * Why do we need this?
     * --------------------
     * The role determines which endpoints the user is allowed
     * to access.
     *
     * Example:
     * --------
     * DOCTOR -> Can create doctor profile
     * PATIENT -> Cannot access doctor-only endpoints
     */
    public String extractRole(String token) {

        return extractAllClaims(token)
                .get("role", String.class);
    }

    /*
     * What does this method do?
     * -------------------------
     * Checks whether a JWT is valid.
     *
     * Why do we need this?
     * --------------------
     * Before trusting any request, we must ensure the token
     * has not been tampered with and is still valid.
     *
     * If the token cannot be parsed successfully,
     * this method returns false.
     */
    public boolean isTokenValid(String token) {

        try {

            // Successfully reading the claims means the
            // JWT is valid.
            extractAllClaims(token);

            return true;

        } catch (Exception ex) {

            // Any parsing error indicates an invalid JWT.
            return false;
        }
    }

}
