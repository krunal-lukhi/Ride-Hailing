package com.krunal.ride.controller;

import com.krunal.ride.dto.LocationDTO;
import com.krunal.ride.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    @PostMapping("/{id}/location")
    public ResponseEntity<Void> updateLocation(@PathVariable Long id, @RequestBody @Valid LocationDTO location) {
        driverService.updateLocation(id, location);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<Void> acceptRide(@PathVariable Long id, @RequestParam Long rideId) {
        driverService.acceptRide(id, rideId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/nearby")
    public ResponseEntity<java.util.List<LocationDTO>> getNearbyDrivers(@RequestParam Double lat,
            @RequestParam Double lon, @RequestParam(defaultValue = "5.0") Double radius) {
        return ResponseEntity.ok(driverService.findNearbyDrivers(lat, lon, radius));
    }
}
