package org.example.backendweride.platform.booking.interfaces.transform;

import org.example.backendweride.platform.booking.infraestructure.persistence.jpa.BookingEntity;
import org.example.backendweride.platform.booking.interfaces.resources.BookingResource;
import org.example.backendweride.platform.booking.domain.services.VehicleCatalogService;

public class BookingResourceFromEntityAssembler {

    public static BookingResource toResource(BookingEntity e) {
        if (e == null) return null;

        VehicleCatalogService.Vehicle vehicle = null;
        var catalog = new VehicleCatalogService();
        var opt = catalog.findById(e.getVehicleId());
        if (opt.isPresent()) vehicle = opt.get();

        String brand = vehicle != null ? vehicle.brand() : null;
        String model = vehicle != null ? vehicle.model() : null;

        return new BookingResource(
            e.getId(),
            e.getCustomerId(),
            e.getVehicleId(),
            brand,
            model,
            e.getDate(),
            e.getUnlockTime(),
            e.getDurationMinutes(),
            e.getPricePerMinute(),
            e.getTotalPrice(),
            e.getStatus()
        );
    }
}
