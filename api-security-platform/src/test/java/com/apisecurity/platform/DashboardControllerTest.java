package com.apisecurity.platform;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetDashboardSummary() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalApplications", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.totalEvents", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.totalThreats", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.criticalThreats", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.highThreats", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.credentialStuffingThreats", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.bolaThreats", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.idEnumerationThreats", greaterThanOrEqualTo(0)));
    }
}
