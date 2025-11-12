package com.weride.platform.garage.infrastructure.persistence.jpa.repositories;

import com.weride.platform.garage.domain.model.aggregates.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {}
