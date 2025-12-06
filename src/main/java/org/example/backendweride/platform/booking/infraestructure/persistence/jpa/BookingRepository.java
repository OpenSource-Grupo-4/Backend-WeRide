package org.example.backendweride.platform.booking.infraestructure.persistence.jpa;

import org.example.backendweride.platform.booking.domain.model.aggregates.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository interface for managing Booking persistence.
 *
 * @summary This repository provides methods to perform CRUD operations and custom queries on Booking aggregate.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingId(Long bookingId);

    Page<Booking> findByUserId(Long userId, Pageable pageable);

    Page<Booking> findByUserIdAndStatus(Long userId, String status, Pageable pageable);

    Page<Booking> findByStatus(String status, Pageable pageable);

    Page<Booking> findByStartDateBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<Booking> findByVehicleId(Long vehicleId, Pageable pageable);

}
