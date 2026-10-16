package com.healthcare.userservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc
class ProfileApiTests extends ApiTestSupport {
    @Test void profileBelongsToTokenIdentity() throws Exception {
        mvc.perform(post("/api/user-profiles").header("Authorization", token(101, "PATIENT"))
                .contentType("application/json").content("""
                {"authUserId":999,"name":"Patient","phone":"5551234567","address":"Demo clinic","gender":"unspecified","dateOfBirth":"1990-01-01"}
                """)).andExpect(status().isCreated()).andExpect(jsonPath("$.authUserId").value(101));
        mvc.perform(get("/api/user-profiles/me").header("Authorization", token(999, "PATIENT")))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/user-profiles/me").header("Authorization", token(101, "PATIENT")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.authUserId").value(101));
    }
    @Test void profileUpdateSupportsPortalAndExistingClients() throws Exception {
        String payload = """
                {"name":"Profile Update","address":"Synthetic address","gender":"unspecified"}
                """;
        mvc.perform(post("/api/user-profiles").header("Authorization", token(202, "PATIENT"))
                .contentType("application/json").content(payload)).andExpect(status().isCreated());
        mvc.perform(put("/api/user-profiles/me").header("Authorization", token(202, "PATIENT"))
                .contentType("application/json").content(payload)).andExpect(status().isOk());
        mvc.perform(patch("/api/user-profiles/me").header("Authorization", token(202, "PATIENT"))
                .contentType("application/json").content(payload)).andExpect(status().isOk());
    }
    @Test void rejectsMissingMalformedAndWrongRoleTokens() throws Exception {
        mvc.perform(get("/api/user-profiles/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/user-profiles/me").header("Authorization", "Bearer"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/user-profiles/me").header("Authorization", "Bearer broken"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/user-profiles/me").header("Authorization", token(2, "DOCTOR")))
                .andExpect(status().isForbidden());
    }
    @Test void documentationWorks() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andExpect(jsonPath("$.openapi").exists());
    }
}
