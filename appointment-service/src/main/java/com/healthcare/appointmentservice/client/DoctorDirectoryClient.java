package com.healthcare.appointmentservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;

/** Appointment-service asks the owning service about doctors instead of reading its database. */
@Component
public class DoctorDirectoryClient {
    private final RestClient client;

    public DoctorDirectoryClient(@Value("${services.doctor.url:http://localhost:8083}") String url) {
        var transport = new SimpleClientHttpRequestFactory();
        // A failed dependency must not leave request threads waiting indefinitely.
        transport.setConnectTimeout(2000);
        transport.setReadTimeout(3000);
        client = RestClient.builder().baseUrl(url).requestFactory(transport).build();
    }

    public DoctorInfo getDoctor(Long doctorId, String authorization) {
        try {
            DoctorInfo doctor = client.get().uri("/api/doctors/{id}", doctorId)
                    .header(HttpHeaders.AUTHORIZATION, authorization).retrieve().body(DoctorInfo.class);
            if (doctor == null || doctor.authUserId() == null) {
                throw new ResponseStatusException(SERVICE_UNAVAILABLE, "Doctor directory returned an invalid response");
            }
            return doctor;
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ResponseStatusException(NOT_FOUND, "Doctor not found");
            }
            throw new ResponseStatusException(SERVICE_UNAVAILABLE, "Doctor directory unavailable");
        } catch (ResourceAccessException ex) {
            throw new ResponseStatusException(SERVICE_UNAVAILABLE, "Doctor directory unavailable; try again later");
        }
    }

    public record DoctorInfo(Long id, Long authUserId, boolean available) {}
}
