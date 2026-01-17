package com.krunal.ride.controller;

import com.krunal.ride.dto.PaymentRequestDTO;
import com.krunal.ride.entity.Payment;
import com.krunal.ride.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Payment> processPayment(@RequestBody @Valid PaymentRequestDTO request) {
        return ResponseEntity.ok(paymentService.processPayment(request));
    }
}
