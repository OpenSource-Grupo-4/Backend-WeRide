package org.example.backendweride.platform.garage.application.internal.queryservices;

import org.example.backendweride.platform.garage.domain.model.aggregates.Vehicle;
import org.example.backendweride.platform.garage.infrastructure.persistence.jpa.repositories.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleQueryServiceImpl {
    private final VehicleRepository repository;

    public List<Vehicle> handle() { return repository.findAll(); }
}
