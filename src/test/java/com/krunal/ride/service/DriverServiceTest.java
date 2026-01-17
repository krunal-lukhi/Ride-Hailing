package com.krunal.ride.service;

import com.krunal.ride.dto.LocationDTO;
import com.krunal.ride.entity.Ride;
import com.krunal.ride.entity.User;
import com.krunal.ride.repository.RideRepository;
import com.krunal.ride.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.geo.Point;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private GeoOperations<String, String> geoOperations;
    @Mock
    private RideRepository rideRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private DriverService driverService;

    @BeforeEach
    void setUp() {
        // Proper mocking of Redis GeoOps template chain
        lenient().when(redisTemplate.opsForGeo()).thenReturn(geoOperations);
    }

    @Test
    void updateLocation_ShouldAddGeoPointToRedis() {
        LocationDTO location = new LocationDTO(10.0, 20.0);
        driverService.updateLocation(1L, location);

        verify(geoOperations).add(eq("drivers:geo"), any(Point.class), eq("1"));
    }

    @Test
    void acceptRide_Success() {
        Ride ride = Ride.builder().id(100L).status(Ride.RideStatus.REQUESTED).build();
        User driver = User.builder().id(2L).role(User.Role.DRIVER).build();

        when(rideRepository.findByIdWithLock(100L)).thenReturn(Optional.of(ride));
        when(userRepository.findById(2L)).thenReturn(Optional.of(driver));

        driverService.acceptRide(2L, 100L);

        assertEquals(Ride.RideStatus.ACCEPTED, ride.getStatus());
        assertEquals(driver, ride.getDriver());
        verify(rideRepository).save(ride);
    }

    @Test
    void acceptRide_RideNotRequested() {
        Ride ride = Ride.builder().id(100L).status(Ride.RideStatus.ACCEPTED).build();
        User driver = User.builder().id(2L).role(User.Role.DRIVER).build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(driver));
        when(rideRepository.findByIdWithLock(100L)).thenReturn(Optional.of(ride));

        assertThrows(RuntimeException.class, () -> driverService.acceptRide(2L, 100L));
    }

    @Test
    void acceptRide_UserNotDriver() {
        Ride ride = Ride.builder().id(100L).status(Ride.RideStatus.REQUESTED).build();
        User rider = User.builder().id(2L).role(User.Role.RIDER).build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(rider));

        assertThrows(RuntimeException.class, () -> driverService.acceptRide(2L, 100L));
    }
}
