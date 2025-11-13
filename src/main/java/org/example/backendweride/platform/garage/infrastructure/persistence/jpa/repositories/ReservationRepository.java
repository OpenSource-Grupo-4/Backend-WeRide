package org.example.backendweride.platform.garage.infrastructure.persistence.jpa.repositories;

import org.example.backendweride.platform.garage.domain.model.aggregates.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {}
