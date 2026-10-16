package com.healthcare.authservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JsonNode;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;

abstract class ApiTestSupport {
    @Autowired protected MockMvc mvc;
    protected final ObjectMapper json = new ObjectMapper();

    protected JsonNode body(MvcResult result) throws Exception {
        return json.readTree(result.getResponse().getContentAsString());
    }
    protected String token(long userId, String role) {
        return "Bearer " + Jwts.builder().subject("user" + userId + "@example.test")
                .claim("authUserId", userId).claim("role", role)
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(Keys.hmacShaKeyFor("test-only-signing-key-at-least-32-bytes-long".getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
