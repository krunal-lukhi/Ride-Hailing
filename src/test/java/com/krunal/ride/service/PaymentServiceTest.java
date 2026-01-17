package com.krunal.ride.service;

import com.krunal.ride.dto.PaymentRequestDTO;
import com.krunal.ride.entity.Payment;
import com.krunal.ride.entity.Ride;
import com.krunal.ride.repository.PaymentRepository;
import com.krunal.ride.repository.RideRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private RideRepository rideRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void processPayment_Success() {
        Ride ride = Ride.builder().id(100L).build();
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setRideId(100L);
        request.setAmount(BigDecimal.TEN);

        when(rideRepository.findById(100L)).thenReturn(Optional.of(ride));
        when(paymentRepository.findByRide(ride)).thenReturn(Optional.empty());

        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            p.setId(1L);
            return p;
        });

        Payment payment = paymentService.processPayment(request);

        assertNotNull(payment);
        assertEquals(Payment.PaymentStatus.COMPLETED, payment.getStatus()); // Mock always succeeds
        assertEquals(BigDecimal.TEN, payment.getAmount());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void processPayment_Duplicate() {
        Ride ride = Ride.builder().id(100L).build();
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setRideId(100L);

        when(rideRepository.findById(100L)).thenReturn(Optional.of(ride));
        when(paymentRepository.findByRide(ride)).thenReturn(Optional.of(new Payment()));

        assertThrows(RuntimeException.class, () -> paymentService.processPayment(request));
        verify(paymentRepository, never()).save(any());
    }
}
