package com.krunal.ride.service;

import com.krunal.ride.dto.PaymentRequestDTO;
import com.krunal.ride.entity.Payment;
import com.krunal.ride.entity.Ride;
import com.krunal.ride.repository.PaymentRepository;
import com.krunal.ride.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RideRepository rideRepository;

    @Transactional
    public Payment processPayment(PaymentRequestDTO request) {
        Ride ride = rideRepository.findById(request.getRideId())
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        // Check if payment already exists
        if (paymentRepository.findByRide(ride).isPresent()) {
            throw new RuntimeException("Payment already processed for this ride");
        }

        // Mock PSP Call
        boolean success = mockPspCall(request.getAmount());

        Payment payment = Payment.builder()
                .ride(ride)
                .amount(request.getAmount())
                .status(success ? Payment.PaymentStatus.COMPLETED : Payment.PaymentStatus.FAILED)
                .externalTransactionId(UUID.randomUUID().toString())
                .build();

        return paymentRepository.save(payment);
    }

    private boolean mockPspCall(java.math.BigDecimal amount) {
        // Simulate processing time
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return true; // Always succeed for demo
    }
}
