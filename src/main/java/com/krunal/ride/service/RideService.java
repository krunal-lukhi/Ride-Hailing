package com.krunal.ride.service;

import com.krunal.ride.dto.RideRequestDTO;
import com.krunal.ride.entity.Ride;
import com.krunal.ride.entity.User;
import com.krunal.ride.event.RideRequestedEvent;
import com.krunal.ride.repository.RideRepository;
import com.krunal.ride.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

@Service
public class RideService {

    private final RideRepository rideRepository;
    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final RideProducer rideProducer;

    @Autowired
    public RideService(RideRepository rideRepository, UserRepository userRepository, StringRedisTemplate redisTemplate, SimpMessagingTemplate messagingTemplate, RideProducer rideProducer) {
        this.rideRepository = rideRepository;
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
        this.rideProducer = rideProducer;
    }

    private static final String DRIVER_GEO_KEY = "drivers:geo";

    @Transactional
    public Ride createRide(RideRequestDTO request) {
        User rider = userRepository.findById(request.getRiderId())
                .orElseThrow(() -> new RuntimeException("Rider not found"));

        if (rideRepository.existsByRiderIdAndStatusIn(rider.getId(), List.of(Ride.RideStatus.REQUESTED, Ride.RideStatus.ACCEPTED, Ride.RideStatus.IN_PROGRESS))) {
            throw new RuntimeException("Rider already has an active ride");
        }

        Ride ride = Ride.builder()
                .rider(rider)
                .pickupLat(request.getPickupLat())
                .pickupLon(request.getPickupLon())
                .dropoffLat(request.getDropoffLat())
                .dropoffLon(request.getDropoffLon())
                .status(Ride.RideStatus.REQUESTED)
                .fare(calculateFare(request)) // Mock fare calculation
                .build();

        Ride savedRide = rideRepository.save(ride);
        
        // Publish Event to Kafka
        RideRequestedEvent event = new RideRequestedEvent(
            savedRide.getId(), 
            rider.getId(), 
            request.getPickupLat(), 
            request.getPickupLon(), 
            request.getDropoffLat(), 
            request.getDropoffLon(),
            savedRide.getFare()
        );
        rideProducer.sendRideRequest(event);

        return savedRide;
    }

    public Ride getRide(Long id) {
        return rideRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ride not found"));
    }

    @Transactional
    public Ride endTrip(Long id) {
        Ride ride = getRide(id);
        if (ride.getStatus() != Ride.RideStatus.IN_PROGRESS && ride.getStatus() != Ride.RideStatus.ACCEPTED) {
            throw new RuntimeException("Ride cannot be ended");
        }
        ride.setStatus(Ride.RideStatus.COMPLETED);
        Ride savedRide = rideRepository.save(ride);

        // Broadcast update
        messagingTemplate.convertAndSend("/topic/ride/" + ride.getId(), savedRide);

        return savedRide;
    }

    private BigDecimal calculateFare(RideRequestDTO request) {
        // Simple mock fare: $10 base + distance factor (sketchy math but sufficient for
        // mock)
        double dist = Math.sqrt(Math.pow(request.getDropoffLat() - request.getPickupLat(), 2) +
                Math.pow(request.getDropoffLon() - request.getPickupLon(), 2));
        return BigDecimal.valueOf(10 + (dist * 100));
    }

    // Helper to find nearby drivers (could be used to notify them)
    public List<String> findNearbyDrivers(Double lat, Double lon, double radiusKm) {
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo()
                .radius(DRIVER_GEO_KEY, new Circle(new Point(lon, lat), new Distance(radiusKm, Metrics.KILOMETERS)));

        return results.getContent().stream()
                .map(geoResult -> geoResult.getContent().getName())
                .toList();
    }
}
