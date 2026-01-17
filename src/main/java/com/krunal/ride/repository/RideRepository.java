package com.krunal.ride.repository;

import com.krunal.ride.entity.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RideRepository extends JpaRepository<Ride, Long> {
    List<Ride> findByRiderId(Long riderId);

    List<Ride> findByDriverId(Long driverId);

    List<Ride> findByStatus(Ride.RideStatus status);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT r FROM Ride r WHERE r.id = :id")
    java.util.Optional<Ride> findByIdWithLock(Long id);

    boolean existsByRiderIdAndStatusIn(Long riderId, List<Ride.RideStatus> statuses);
}
