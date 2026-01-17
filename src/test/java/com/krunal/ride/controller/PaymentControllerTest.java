package com.krunal.ride.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krunal.ride.dto.PaymentRequestDTO;
import com.krunal.ride.entity.Payment;
import com.krunal.ride.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@org.springframework.context.annotation.Import({com.krunal.ride.config.SecurityConfig.class, com.krunal.ride.security.ApiKeyAuthFilter.class})
@org.springframework.test.context.TestPropertySource(properties = "ride.api-key=test-key")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void processPayment_Success() throws Exception {
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setRideId(100L);
        request.setAmount(BigDecimal.TEN);

        Payment payment = Payment.builder()
                .id(1L)
                .amount(BigDecimal.TEN)
                .status(Payment.PaymentStatus.COMPLETED)
                .build();

        when(paymentService.processPayment(any(PaymentRequestDTO.class))).thenReturn(payment);

        mockMvc.perform(post("/v1/payments")
                .header("X-API-KEY", "test-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}
