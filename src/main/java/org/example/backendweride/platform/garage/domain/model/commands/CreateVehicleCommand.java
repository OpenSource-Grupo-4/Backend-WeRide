package org.example.backendweride.platform.garage.domain.model.commands;
import java.util.Date;
import java.util.List;
public record CreateVehicleCommand(
        String brand, String model, Integer year, Integer battery, Integer maxSpeed,
        Integer range, Double weight, String color, String licensePlate, String location,
        String status, String type, String companyId, Double pricePerMinute, String image,
        List<String> features, String maintenanceStatus, Date lastMaintenance,
        Date nextMaintenance, Double totalKilometers, Double rating
) {}