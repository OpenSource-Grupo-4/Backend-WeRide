package org.example.backendweride.platform.garage.application.internal.commandservices;

import lombok.RequiredArgsConstructor;
import org.example.backendweride.platform.garage.domain.model.aggregates.Vehicle;
import org.example.backendweride.platform.garage.infrastructure.persistence.jpa.repositories.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RemoveFavoriteCommandService {
    private final VehicleRepository vehicleRepository;

    @Transactional
    public Vehicle handle(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        vehicle.setFavorite(false);
        return vehicleRepository.save(vehicle);
    }
}
