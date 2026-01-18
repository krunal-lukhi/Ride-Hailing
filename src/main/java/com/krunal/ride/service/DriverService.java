package com.krunal.ride.service;

import com.krunal.ride.dto.LocationDTO;
import com.krunal.ride.entity.Ride;
import com.krunal.ride.entity.User;
import com.krunal.ride.repository.RideRepository;
import com.krunal.ride.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DriverService {

    private final UserRepository userRepository;
    private final RideRepository rideRepository;
    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public DriverService(UserRepository userRepository, RideRepository rideRepository,
            StringRedisTemplate redisTemplate, SimpMessagingTemplate messagingTemplate) {
        this.userRepository = userRepository;
        this.rideRepository = rideRepository;
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
    }

    private static final String DRIVER_GEO_KEY = "drivers:geo";

    public void updateLocation(Long driverId, LocationDTO location) {
        redisTemplate.opsForGeo().add(DRIVER_GEO_KEY, new Point(location.getLongitude(), location.getLatitude()),
                driverId.toString());
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DriverService.class);

    // Helper to find nearby drivers
    public List<LocationDTO> findNearbyDrivers(Double lat, Double lon, double radiusKm) {
        log.info("Finding nearby drivers at lat: {}, lon: {}, radius: {}", lat, lon, radiusKm);
        try {
            GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo()
                    .radius(DRIVER_GEO_KEY, new Circle(new Point(lon, lat), new Distance(radiusKm, Metrics.KILOMETERS)));

            if (results == null) {
                log.warn("Redis returned null results");
                return List.of();
            }

            return results.getContent().stream()
                    .map(geoResult -> {
                        Point point = geoResult.getContent().getPoint();
                        if (point == null) {
                            return null;
                        }
                        LocationDTO dto = new LocationDTO();
                        dto.setLatitude(point.getY());
                        dto.setLongitude(point.getX());
                        return dto;
                    })
                    .filter(java.util.Objects::nonNull)
                    .toList();
        } catch (Exception e) {
            log.error("Error finding nearby drivers", e);
            return List.of();
        }
    }

    @Transactional
    public void acceptRide(Long driverId, Long rideId) {
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        if (driver.getRole() != User.Role.DRIVER) {
            throw new RuntimeException("User is not a driver");
        }

        Ride ride = rideRepository.findByIdWithLock(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        if (ride.getStatus() != Ride.RideStatus.REQUESTED) {
            throw new RuntimeException("Ride already accepted or cancelled");
        }

        ride.setDriver(driver);
        ride.setStatus(Ride.RideStatus.ACCEPTED);
        Ride savedRide = rideRepository.save(ride);

        // Notify subscribers
        messagingTemplate.convertAndSend("/topic/ride/" + ride.getId(), savedRide);
    }
}
