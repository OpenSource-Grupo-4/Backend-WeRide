package org.example.backendweride.platform.booking.domain.services;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.Objects;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;

/**
 * Service for managing the vehicle catalog.
 *
 * @summary This service provides access to vehicle information such as id, brand, model, type, and pricing.
 */
@Service
public class VehicleCatalogService {

    public record Vehicle(String id, String brand, String model, String type, BigDecimal pricePerMinute) { }

    private final Map<String, Vehicle> byId = new HashMap<>();

    public VehicleCatalogService() {
        // Initialize in JSON format for reference:
        // Simplified view of the provided JSON (id + pricePerMinute + metadata)
        byId.put("1", new Vehicle("1","Xiaomi","M365","electric_scooter", new BigDecimal("0.5")));
        byId.put("2", new Vehicle("2","Trek","FX 3","bike", new BigDecimal("0.3")));
        byId.put("3", new Vehicle("3","Segway","Ninebot Max","electric_scooter", new BigDecimal("0.6")));
        byId.put("4", new Vehicle("4","Giant","Escape 3","bike", new BigDecimal("0.35")));
        byId.put("5", new Vehicle("5","Ninebot","ES2","electric_scooter", new BigDecimal("0.45")));
        byId.put("6", new Vehicle("6","Specialized","Sirrus X","bike", new BigDecimal("0.4")));
        byId.put("7", new Vehicle("7","Xiaomi","Pro 2","electric_scooter", new BigDecimal("0.55")));
        byId.put("8", new Vehicle("8","Segway","E22","electric_scooter", new BigDecimal("0.5")));
        byId.put("9", new Vehicle("9","Razor","E300","electric_scooter", new BigDecimal("0.4")));
        byId.put("10", new Vehicle("10","Unagi","Model One","electric_scooter", new BigDecimal("0.65")));
    }

    /**
     * Find a vehicle by its id.
     */
    public Optional<Vehicle> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    /**
     * Find vehicles by their type (example: "electric_scooter", "bike"). Returns an ordered list.
     */
    public List<Vehicle> findByType(String type) {
        Objects.requireNonNull(type, "type");
        List<Vehicle> result = new ArrayList<>();
        for (Vehicle v : byId.values()) {
            if (type.equalsIgnoreCase(v.type())) result.add(v);
        }
        return result;
    }

}
