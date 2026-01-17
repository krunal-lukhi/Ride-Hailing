package com.krunal.ride.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class RideRequestDTO {
    @NotNull
    private Long riderId;
    @NotNull
    private Double pickupLat;
    @NotNull
    private Double pickupLon;
    @NotNull
    private Double dropoffLat;
    @NotNull
    private Double dropoffLon;
    private String paymentMethod;
}
