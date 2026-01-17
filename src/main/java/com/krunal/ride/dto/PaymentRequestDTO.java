package com.krunal.ride.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class PaymentRequestDTO {
    @NotNull
    private Long rideId;
    @NotNull
    private BigDecimal amount;
}
