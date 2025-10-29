package org.example.backendweride.platform.booking.infraestructure.persistence.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, String> {

    Page<BookingEntity> findByCustomerId(String customerId, Pageable pageable);

    Page<BookingEntity> findByCustomerIdAndStatus(String customerId, String status, Pageable pageable);

    Page<BookingEntity> findByStatus(String status, Pageable pageable);

    Page<BookingEntity> findByDateBetween(LocalDate from, LocalDate to, Pageable pageable);

    Page<BookingEntity> findByVehicleId(String vehicleId, Pageable pageable);

}
