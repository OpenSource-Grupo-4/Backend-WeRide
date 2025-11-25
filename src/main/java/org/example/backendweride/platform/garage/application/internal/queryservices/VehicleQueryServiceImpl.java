package org.example.backendweride.platform.garage.application.internal.queryservices;

import lombok.RequiredArgsConstructor;
import org.example.backendweride.platform.garage.domain.model.aggregates.Vehicle;
import org.example.backendweride.platform.garage.infrastructure.persistence.jpa.repositories.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleQueryServiceImpl {
    private final VehicleRepository vehicleRepository;

    public List<Vehicle> handle() {
        return vehicleRepository.findAll();
    }

    public Optional<Vehicle> handleById(Long id) {
        return vehicleRepository.findById(id);
    }

    public List<Vehicle> handleFavorites() {
        return vehicleRepository.findAll().stream()
                .filter(Vehicle::isFavorite)
                .collect(Collectors.toList());
    }

    public List<Vehicle> handleSearch(String brand, String model, Integer year) {
        return vehicleRepository.findAll().stream()
                .filter(v -> brand == null || v.getBrand().equalsIgnoreCase(brand))
                .filter(v -> model == null || v.getModel().equalsIgnoreCase(model))
                .filter(v -> year == null || v.getYear().equals(year))
                .collect(Collectors.toList());
    }
}
