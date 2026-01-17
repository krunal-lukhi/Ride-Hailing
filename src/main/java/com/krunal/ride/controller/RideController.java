package com.krunal.ride.controller;

import com.krunal.ride.dto.RideRequestDTO;
import com.krunal.ride.entity.Ride;
import com.krunal.ride.service.RideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class RideController {

    private final RideService rideService;

    @PostMapping("/rides")
    public ResponseEntity<Ride> createRide(@RequestBody @Valid RideRequestDTO request) {
        return ResponseEntity.ok(rideService.createRide(request));
    }

    @GetMapping("/rides/{id}")
    public ResponseEntity<Ride> getRide(@PathVariable Long id) {
        return ResponseEntity.ok(rideService.getRide(id));
    }

    @PostMapping("/trips/{id}/end")
    public ResponseEntity<Ride> endTrip(@PathVariable Long id) {
        return ResponseEntity.ok(rideService.endTrip(id));
    }
}
