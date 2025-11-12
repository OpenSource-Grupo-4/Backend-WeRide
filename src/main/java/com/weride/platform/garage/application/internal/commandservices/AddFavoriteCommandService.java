package com.weride.platform.garage.application.internal.commandservices;

import com.weride.platform.garage.domain.exceptions.VehicleNotFoundException;
import com.weride.platform.garage.domain.model.aggregates.Vehicle;
import com.weride.platform.garage.infrastructure.persistence.jpa.repositories.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AddFavoriteCommandService {
    private final VehicleRepository vehicleRepository;

    public Vehicle handle(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new VehicleNotFoundException(vehicleId));
        vehicle.setFavorite(true);
        return vehicleRepository.save(vehicle);
    }
}
