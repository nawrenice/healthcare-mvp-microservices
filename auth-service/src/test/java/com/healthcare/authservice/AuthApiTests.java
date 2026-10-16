package com.healthcare.authservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc
class AuthApiTests extends ApiTestSupport {
    @Test void signupLoginAndTokenIdentity() throws Exception {
        mvc.perform(post("/api/auth/signup").contentType("application/json").content("""
                {"name":"Patient One","email":"Patient.One@example.test","password":"LongPassword123!","role":"PATIENT"}
                """)).andExpect(status().isCreated());
        var login = mvc.perform(post("/api/auth/login").contentType("application/json").content("""
                {"email":"PATIENT.ONE@example.test","password":"LongPassword123!"}
                """)).andExpect(status().isOk()).andReturn();
        var claims = Jwts.parser().verifyWith(Keys.hmacShaKeyFor(
                "test-only-signing-key-at-least-32-bytes-long".getBytes(StandardCharsets.UTF_8)))
                .build().parseSignedClaims(body(login).get("token").asText()).getPayload();
        assertThat(((Number) claims.get("authUserId")).longValue()).isPositive();
        assertThat(claims.get("role")).isEqualTo("PATIENT");
        mvc.perform(post("/api/auth/login").contentType("application/json").content("""
                {"email":"patient.one@example.test","password":"incorrect"}
                """)).andExpect(status().isUnauthorized());
    }
    @Test void publicSignupCannotGrantStaffPrivileges() throws Exception {
        for (String role : new String[]{"ADMIN", "DOCTOR", "RECEPTION"}) {
            mvc.perform(post("/api/auth/signup").contentType("application/json").content(
                    "{\"name\":\"Staff\",\"email\":\"staff@example.test\",\"password\":\"LongPassword123!\",\"role\":\"" + role + "\"}"))
                    .andExpect(status().isBadRequest());
        }
    }
    @Test void documentationAndHealthAreAvailable() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andExpect(jsonPath("$.openapi").exists());
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }
}
