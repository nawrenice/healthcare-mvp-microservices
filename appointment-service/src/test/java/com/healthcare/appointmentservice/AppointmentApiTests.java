package com.healthcare.appointmentservice;

import com.healthcare.appointmentservice.client.DoctorDirectoryClient;
import com.healthcare.appointmentservice.entity.*;
import com.healthcare.appointmentservice.repository.AppointmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest @AutoConfigureMockMvc
class AppointmentApiTests extends ApiTestSupport {
    @MockitoBean DoctorDirectoryClient directory;
    @Autowired AppointmentRepository appointments;

    private Instant slot(int days) {
        long seconds = Instant.now().plus(days, ChronoUnit.DAYS).getEpochSecond();
        return Instant.ofEpochSecond((seconds / 1800 + 1) * 1800);
    }
    private String request(Instant start, long doctor) {
        return "{\"doctorId\":" + doctor + ",\"startsAt\":\"" + start + "\",\"reason\":\"Synthetic checkup\"}";
    }
    private void available(long doctorId, long owner) {
        when(directory.getDoctor(eq(doctorId), anyString())).thenReturn(new DoctorDirectoryClient.DoctorInfo(doctorId, owner, true));
    }
    @Test void ownershipCancellationAndRebooking() throws Exception {
        available(10, 20);
        String payload = request(slot(1), 10);
        var created = mvc.perform(post("/api/appointments").header("Authorization", token(11, "PATIENT"))
                .contentType("application/json").content(payload)).andExpect(status().isCreated()).andReturn();
        long id = body(created).get("id").asLong();
        mvc.perform(get("/api/appointments/" + id).header("Authorization", token(12, "PATIENT")))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/appointments/" + id + "/cancel").header("Authorization", token(21, "DOCTOR")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/appointments/" + id).header("Authorization", token(20, "DOCTOR")))
                .andExpect(status().isOk());
        mvc.perform(patch("/api/appointments/" + id + "/cancel").header("Authorization", token(11, "PATIENT")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELLED"));
        mvc.perform(post("/api/appointments").header("Authorization", token(12, "PATIENT"))
                .contentType("application/json").content(payload)).andExpect(status().isCreated());
    }
    @Test void concurrentRequestsCannotDoubleBookDoctor() throws Exception {
        available(30, 40);
        String payload = request(slot(2), 30);
        var barrier = new CyclicBarrier(2);
        try (var workers = Executors.newFixedThreadPool(2)) {
            Callable<Integer> first = () -> {
                barrier.await(5, TimeUnit.SECONDS);
                return mvc.perform(post("/api/appointments").header("Authorization", token(31, "PATIENT"))
                        .contentType("application/json").content(payload)).andReturn().getResponse().getStatus();
            };
            Callable<Integer> second = () -> {
                barrier.await(5, TimeUnit.SECONDS);
                return mvc.perform(post("/api/appointments").header("Authorization", token(32, "PATIENT"))
                        .contentType("application/json").content(payload)).andReturn().getResponse().getStatus();
            };
            var a = workers.submit(first);
            var b = workers.submit(second);
            assertThat(java.util.List.of(a.get(10, TimeUnit.SECONDS), b.get(10, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(201, 409);
        }
    }
    @Test void patientCannotBookTwoDoctorsAtSameTime() throws Exception {
        available(50, 60); available(51, 61);
        Instant start = slot(3);
        mvc.perform(post("/api/appointments").header("Authorization", token(51, "PATIENT"))
                .contentType("application/json").content(request(start, 50))).andExpect(status().isCreated());
        mvc.perform(post("/api/appointments").header("Authorization", token(51, "PATIENT"))
                .contentType("application/json").content(request(start, 51))).andExpect(status().isConflict());
    }
    @Test void validatesTimeAvailabilityAndDependencyFailures() throws Exception {
        available(70, 80);
        mvc.perform(post("/api/appointments").header("Authorization", token(71, "PATIENT"))
                .contentType("application/json").content(request(slot(4).plusSeconds(1), 70))).andExpect(status().isBadRequest());
        mvc.perform(post("/api/appointments").header("Authorization", token(71, "PATIENT"))
                .contentType("application/json").content(request(Instant.now().minusSeconds(3600), 70))).andExpect(status().isBadRequest());
        when(directory.getDoctor(eq(70L), anyString())).thenReturn(new DoctorDirectoryClient.DoctorInfo(70L, 80L, false));
        mvc.perform(post("/api/appointments").header("Authorization", token(71, "PATIENT"))
                .contentType("application/json").content(request(slot(4), 70))).andExpect(status().isConflict());
        when(directory.getDoctor(eq(70L), anyString())).thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "Directory unavailable"));
        mvc.perform(post("/api/appointments").header("Authorization", token(71, "PATIENT"))
                .contentType("application/json").content(request(slot(4), 70))).andExpect(status().isServiceUnavailable());
        mvc.perform(post("/api/appointments").header("Authorization", token(80, "DOCTOR"))
                .contentType("application/json").content(request(slot(4), 70))).andExpect(status().isForbidden());
    }
    @Test void onlyAssignedDoctorCanCompleteAnEndedVisit() throws Exception {
        Appointment past = appointments.saveAndFlush(Appointment.builder().patientAuthUserId(91L).doctorAuthUserId(92L)
                .doctorId(90L).startsAt(Instant.now().minusSeconds(7200)).endsAt(Instant.now().minusSeconds(5400))
                .status(AppointmentStatus.BOOKED).reason("Synthetic followup").build());
        mvc.perform(patch("/api/appointments/" + past.getId() + "/complete").header("Authorization", token(91, "PATIENT")))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/appointments/" + past.getId() + "/complete").header("Authorization", token(93, "DOCTOR")))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/appointments/" + past.getId() + "/complete").header("Authorization", token(92, "DOCTOR")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("COMPLETED"));
    }
    @Test void documentationWorks() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andExpect(jsonPath("$.openapi").exists());
    }
}
