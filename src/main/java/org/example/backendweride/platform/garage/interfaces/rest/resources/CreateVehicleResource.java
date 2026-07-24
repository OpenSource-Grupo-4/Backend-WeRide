package org.example.backendweride.platform.garage.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;
import java.util.List;

public record CreateVehicleResource(
        @NotBlank String brand,
        @NotBlank String model,
        Integer year,
        Integer battery,
        Integer maxSpeed,
        Integer range,
        double weight,
        String color,
        String licensePlate,
        String location,
        String status,
        @NotBlank String type,
        String companyId,
        double pricePerMinute,
        String image,
        List<String> features,
        String maintenanceStatus,
        Date lastMaintenance,
        Date nextMaintenance,
        double totalKilometers,
        double rating
) {}