package com.krunal.ride.service;

import com.krunal.ride.dto.LocationDTO;
import com.krunal.ride.event.RideRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingService {

    private final DriverService driverService;

    @KafkaListener(topics = "ride-requests", groupId = "ride-group")
    public void consumeRideRequest(RideRequestedEvent event) {
        log.info("Received Ride Request Event: {}", event);

        // Simulate Matching Logic
        log.info("Searching for drivers near [{}, {}]", event.getPickupLat(), event.getPickupLon());
        
        List<LocationDTO> nearbyDrivers = driverService.findNearbyDrivers(event.getPickupLat(), event.getPickupLon(), 5.0);
        
        if (nearbyDrivers.isEmpty()) {
            log.warn("No drivers found for Ride ID: {}", event.getRideId());
        } else {
             log.info("Found {} drivers. Offering ride...", nearbyDrivers.size());
             // In a real system, we would push notification to these drivers
             // For now, we just log it, as the driver accepts manually via API
        }
    }
}
