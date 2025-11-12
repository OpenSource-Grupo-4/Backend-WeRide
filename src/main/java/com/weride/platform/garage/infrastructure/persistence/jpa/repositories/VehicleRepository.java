package com.weride.platform.garage.infrastructure.persistence.jpa.repositories;

import com.weride.platform.garage.domain.model.aggregates.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {}
