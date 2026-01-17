package com.krunal.ride.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krunal.ride.dto.LocationDTO;
import com.krunal.ride.service.DriverService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DriverController.class)
@org.springframework.context.annotation.Import({com.krunal.ride.config.SecurityConfig.class, com.krunal.ride.security.ApiKeyAuthFilter.class})
@org.springframework.test.context.TestPropertySource(properties = "ride.api-key=test-key")
class DriverControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DriverService driverService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void updateLocation_Success() throws Exception {
        LocationDTO location = new LocationDTO(10.0, 20.0);

        mockMvc.perform(post("/v1/drivers/1/location")
                .header("X-API-KEY", "test-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(location)))
                .andExpect(status().isOk());

        verify(driverService).updateLocation(1L, location);
    }
}
