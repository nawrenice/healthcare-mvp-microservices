package com.healthcare.doctorservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc
class DoctorApiTests extends ApiTestSupport {
    private static final String PROFILE = """
            {"name":"Dr Demo","specialty":"Primary Care","licenseNumber":"DEMO-001","clinicAddress":"Demo clinic","available":true}
            """;
    @Test void onlyDoctorsCanManageTheirOwnDirectoryEntry() throws Exception {
        mvc.perform(post("/api/doctors").header("Authorization", token(1, "PATIENT"))
                .contentType("application/json").content(PROFILE)).andExpect(status().isForbidden());
        var result = mvc.perform(post("/api/doctors").header("Authorization", token(2, "DOCTOR"))
                .contentType("application/json").content(PROFILE)).andExpect(status().isCreated())
                .andExpect(jsonPath("$.authUserId").value(2)).andReturn();
        mvc.perform(post("/api/doctors").header("Authorization", token(2, "DOCTOR"))
                .contentType("application/json").content(PROFILE)).andExpect(status().isConflict());
        mvc.perform(get("/api/doctors/me").header("Authorization", token(2, "DOCTOR")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.licenseNumber").value("DEMO-001"));
        mvc.perform(get("/api/doctors/" + body(result).get("id").asLong()).header("Authorization", token(1, "PATIENT")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.licenseNumber").doesNotExist());
        mvc.perform(get("/api/doctors/me").header("Authorization", token(3, "DOCTOR")))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/doctors").header("Authorization", token(1, "PATIENT")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(body(result).get("id").asLong()));
    }
    @Test void documentationWorks() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andExpect(jsonPath("$.openapi").exists());
    }
}
