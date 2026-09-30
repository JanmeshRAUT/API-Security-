package com.apisecurity.platform;

import com.apisecurity.platform.dto.ApplicationRegistrationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testRegisterApplicationSuccess() throws Exception {
        ApplicationRegistrationRequest req = new ApplicationRegistrationRequest();
        req.setName("Test Service");
        req.setApplicationId("test-service");
        req.setDescription("Generic test service");

        mockMvc.perform(post("/api/v1/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.applicationId", is("shop-sphere-test")))
                .andExpect(jsonPath("$.apiKey", startsWith("sec_")))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    public void testRegisterApplicationInvalidInput() throws Exception {
        ApplicationRegistrationRequest req = new ApplicationRegistrationRequest();
        // Missing name and applicationId

        mockMvc.perform(post("/api/v1/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetAllApplications() throws Exception {
        mockMvc.perform(get("/api/v1/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(instanceOf(java.util.List.class))));
    }
}
