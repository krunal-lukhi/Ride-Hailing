package com.krunal.ride.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krunal.ride.dto.RideRequestDTO;
import com.krunal.ride.entity.Ride;
import com.krunal.ride.service.RideService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RideController.class)
@org.springframework.context.annotation.Import({com.krunal.ride.config.SecurityConfig.class, com.krunal.ride.security.ApiKeyAuthFilter.class})
@org.springframework.test.context.TestPropertySource(properties = "ride.api-key=test-key")
class RideControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RideService rideService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createRide_Success() throws Exception {
        RideRequestDTO request = new RideRequestDTO();
        request.setRiderId(1L);
        request.setPickupLat(10.0);
        request.setPickupLon(20.0);
        request.setDropoffLat(10.1);
        request.setDropoffLon(20.1);
        request.setPaymentMethod("CARD");

        Ride ride = Ride.builder().id(100L).status(Ride.RideStatus.REQUESTED).build();

        when(rideService.createRide(any(RideRequestDTO.class))).thenReturn(ride);

        mockMvc.perform(post("/v1/rides")
                .header("X-API-KEY", "test-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.status").value("REQUESTED"));
    }

    @Test
    void getRide_Success() throws Exception {
        Ride ride = Ride.builder().id(100L).status(Ride.RideStatus.REQUESTED).build();
        when(rideService.getRide(100L)).thenReturn(ride);

        mockMvc.perform(get("/v1/rides/100")
                .header("X-API-KEY", "test-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L));
    }
}
