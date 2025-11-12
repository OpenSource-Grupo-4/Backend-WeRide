package org.example.backendweride.platform.garage.infrastructure.persistence.jpa.repositories;

import org.example.backendweride.platform.garage.domain.model.aggregates.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {}
