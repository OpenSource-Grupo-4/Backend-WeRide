package org.example.backendweride.platform.booking.infraestructure.persistence.jpa;

import org.example.backendweride.platform.booking.domain.model.aggregates.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
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

    Page<Booking> findByUserIdAndVehicleId(Long userId, Long vehicleId, Pageable pageable);

    Page<Booking> findByUserIdAndStartDateBetween(Long userId, LocalDateTime from, LocalDateTime to, Pageable pageable);

    /**
     * Returns true when an active booking already occupies the vehicle for the requested
     * time range. Overlap semantics: existing.start < requested.end AND existing.end > requested.start.
     * endDate is always populated by the aggregate factories (derived from duration when absent).
     *
     * @param vehicleId the vehicle to check
     * @param start     requested start
     * @param end       requested end (must be non-null)
     * @param statuses  the booking statuses that occupy the vehicle
     * @param excludeBookingId booking to ignore (e.g. the booking being updated), or null
     */
    @Query("""
            SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
            FROM Booking b
            WHERE b.vehicleId = :vehicleId
              AND b.status IN :statuses
              AND b.startDate < :end
              AND b.endDate > :start
              AND (:excludeBookingId IS NULL OR b.bookingId <> :excludeBookingId)
            """)
    boolean existsOverlappingBooking(@Param("vehicleId") Long vehicleId,
                                     @Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end,
                                     @Param("statuses") List<String> statuses,
                                     @Param("excludeBookingId") Long excludeBookingId);
}
