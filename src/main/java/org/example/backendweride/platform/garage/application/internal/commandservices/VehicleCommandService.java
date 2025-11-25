package org.example.backendweride.platform.garage.application.internal.commandservices;

import lombok.RequiredArgsConstructor;
import org.example.backendweride.platform.garage.domain.model.aggregates.Vehicle;
import org.example.backendweride.platform.garage.infrastructure.persistence.jpa.repositories.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VehicleCommandService {
    private final VehicleRepository vehicleRepository;

    @Transactional
    public Vehicle handle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    @Transactional
    public Optional<Vehicle> handleUpdate(Long id, Vehicle vehicleData) {
        return vehicleRepository.findById(id)
                .map(existingVehicle -> {
                    existingVehicle.setBrand(vehicleData.getBrand());
                    existingVehicle.setModel(vehicleData.getModel());
                    existingVehicle.setYear(vehicleData.getYear());
                    existingVehicle.setPrice(vehicleData.getPrice());
                    existingVehicle.setDescription(vehicleData.getDescription());
                    return vehicleRepository.save(existingVehicle);
                });
    }

    @Transactional
    public boolean handleDelete(Long id) {
        if (vehicleRepository.existsById(id)) {
            vehicleRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
