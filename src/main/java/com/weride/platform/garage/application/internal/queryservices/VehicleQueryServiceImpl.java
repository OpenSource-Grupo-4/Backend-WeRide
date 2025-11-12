package com.weride.platform.garage.application.internal.queryservices;

import com.weride.platform.garage.domain.model.aggregates.Vehicle;
import com.weride.platform.garage.infrastructure.persistence.jpa.repositories.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleQueryServiceImpl {
    private final VehicleRepository repository;

    public List<Vehicle> handle() { return repository.findAll(); }
}
