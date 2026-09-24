package com.mutuals.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MutualsFlowIntegrationTest {

    private static final String PASSWORD = "Str0ng!Pass";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerValidatesAndRejectsDuplicates() throws Exception {
        String suffix = shortId();
        register("dup" + suffix);

        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "dup" + suffix + "@mail.com", "username", "other" + suffix,
                                "displayName", "Otro", "password", PASSWORD))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/register"));

        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "weak" + suffix + "@mail.com", "username", "weak" + suffix,
                                "displayName", "Weak", "password", "123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    void protectedEndpointsRequireValidToken() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")).andExpect(status().isUnauthorized());

        String suffix = shortId();
        String token = register("me" + suffix);
        mockMvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("me" + suffix));

        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "me" + suffix + "@mail.com", "password", "Wrong!Pass1"))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/admin/metrics").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void mutualsCanStartAStreakAndCheckInWithQr() throws Exception {
        String suffix = shortId();
        String anaToken = register("ana" + suffix);
        String jhanToken = register("jhan" + suffix);
        long anaId = myId(anaToken);
        long jhanId = myId(jhanToken);

        mockMvc.perform(post("/api/v1/users/" + jhanId + "/follow").header("Authorization", "Bearer " + anaToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mutual").value(false));
        mockMvc.perform(post("/api/v1/users/" + anaId + "/follow").header("Authorization", "Bearer " + jhanToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mutual").value(true));

        JsonNode invitation = body(mockMvc.perform(post("/api/v1/streak-invitations")
                        .header("Authorization", "Bearer " + anaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("friendId", jhanId))))
                .andExpect(status().isCreated())
                .andReturn());
        mockMvc.perform(patch("/api/v1/streak-invitations/" + invitation.get("id").asLong())
                        .header("Authorization", "Bearer " + jhanToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("accept", true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        String qrToken = body(mockMvc.perform(get("/api/v1/qr-codes/me").header("Authorization", "Bearer " + jhanToken))
                .andExpect(status().isOk())
                .andReturn()).get("token").asText();
        mockMvc.perform(post("/api/v1/check-ins/qr")
                        .header("Authorization", "Bearer " + anaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("token", qrToken))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.method").value("QR"))
                .andExpect(jsonPath("$.streakLength").value(1));

        JsonNode home = body(mockMvc.perform(get("/api/v1/mutuals").header("Authorization", "Bearer " + anaToken))
                .andExpect(status().isOk())
                .andReturn());
        assertThat(home.get(0).get("streak").get("countedToday").asBoolean()).isTrue();

        mockMvc.perform(delete("/api/v1/users/" + jhanId + "/follow").header("Authorization", "Bearer " + anaToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.details.currentLength").value(1));
        mockMvc.perform(delete("/api/v1/users/" + jhanId + "/follow").param("confirmStreakLoss", "true")
                        .header("Authorization", "Bearer " + anaToken))
                .andExpect(status().isNoContent());
    }

    private String register(String username) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", username + "@mail.com", "username", username,
                                "displayName", username, "password", PASSWORD))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();
        return body(result).get("accessToken").asText();
    }

    private long myId(String token) throws Exception {
        return body(mockMvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + token))
                .andReturn()).get("id").asLong();
    }

    private JsonNode body(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private String shortId() {
        return UUID.randomUUID().toString().substring(0, 8).replace("-", "");
    }
}
