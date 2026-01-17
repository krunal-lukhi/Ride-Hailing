package com.krunal.ride.service;

import com.krunal.ride.dto.RideRequestDTO;
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
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideServiceTest {

    @Mock
    private RideRepository rideRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private RideProducer rideProducer;
    @Mock
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private RideService rideService;

    private User rider;
    private RideRequestDTO rideRequest;

    @BeforeEach
    void setUp() {
        rider = User.builder()
                .id(1L)
                .name("Rider One")
                .email("rider@test.com")
                .role(User.Role.RIDER)
                .build();

        rideRequest = new RideRequestDTO();
        rideRequest.setRiderId(1L);
        rideRequest.setPickupLat(10.0);
        rideRequest.setPickupLon(20.0);
        rideRequest.setDropoffLat(10.1);
        rideRequest.setDropoffLon(20.1);
        rideRequest.setPaymentMethod("CARD");
    }

    @Test
    void createRide_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(rider));
        when(rideRepository.existsByRiderIdAndStatusIn(anyLong(), anyList())).thenReturn(false);
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> {
            Ride r = invocation.getArgument(0);
            r.setId(100L);
            return r;
        });

        // Act
        Ride createdRide = rideService.createRide(rideRequest);

        // Assert
        assertNotNull(createdRide);
        assertEquals(100L, createdRide.getId());
        assertEquals(Ride.RideStatus.REQUESTED, createdRide.getStatus());
        assertEquals(rider, createdRide.getRider());
        assertNotNull(createdRide.getFare());

        verify(userRepository).findById(1L);
        verify(rideRepository).save(any(Ride.class));
    }

    @Test
    void createRide_RiderNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> rideService.createRide(rideRequest));
        verify(rideRepository, never()).save(any());
    }

    @Test
    void getRide_Success() {
        Ride ride = new Ride();
        ride.setId(100L);
        when(rideRepository.findById(100L)).thenReturn(Optional.of(ride));

        Ride found = rideService.getRide(100L);
        assertEquals(100L, found.getId());
    }

    @Test
    void endTrip_Success() {
        Ride ride = Ride.builder()
                .id(100L)
                .status(Ride.RideStatus.IN_PROGRESS)
                .build();

        when(rideRepository.findById(100L)).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenAnswer(i -> i.getArgument(0));

        Ride endedRide = rideService.endTrip(100L);

        assertEquals(Ride.RideStatus.COMPLETED, endedRide.getStatus());
        verify(rideRepository).save(ride);
    }

    @Test
    void endTrip_InvalidStatus() {
        Ride ride = Ride.builder()
                .id(100L)
                .status(Ride.RideStatus.REQUESTED) // Cannot end a requested ride
                .build();

        when(rideRepository.findById(100L)).thenReturn(Optional.of(ride));

        assertThrows(RuntimeException.class, () -> rideService.endTrip(100L));
    }
}
