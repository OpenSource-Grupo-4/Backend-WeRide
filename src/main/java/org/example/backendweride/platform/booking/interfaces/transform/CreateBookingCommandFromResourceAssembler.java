package org.example.backendweride.platform.booking.interfaces.transform;

import org.example.backendweride.platform.booking.interfaces.resources.CreateBookingResource;
import org.example.backendweride.platform.booking.domain.model.commands.CreateBookingCommand;

public class CreateBookingCommandFromResourceAssembler {

    public static CreateBookingCommand toCommand(CreateBookingResource r) {
        if (r == null) return null;
        return new CreateBookingCommand(
            r.id(),
            r.customerId(),
            r.vehicleType(), // here vehicleType contains the vehicle id selected by user
            r.date(),
            r.unlockTime(),
            r.durationMinutes()
        );
    }
}
