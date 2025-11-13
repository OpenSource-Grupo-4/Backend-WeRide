package org.example.backendweride.platform.booking.interfaces.transform;

import org.example.backendweride.platform.booking.interfaces.resources.CreateBookingResource;
import org.example.backendweride.platform.booking.domain.model.commands.CreateBookingCommand;

/**
 * Assembler class to convert CreateBookingResource to CreateBookingCommand.
 *
 * @summary This class provides a method to transform a CreateBookingResource object
 *          into a CreateBookingCommand object for processing booking creation requests.
 */
public class CreateBookingCommandFromResourceAssembler {

    public static CreateBookingCommand toCommand(CreateBookingResource r) {
        if (r == null) return null;
        return new CreateBookingCommand(
            r.id(),
            r.customerId(),
            r.vehicleType(),
            r.date(),
            r.unlockTime(),
            r.durationMinutes()
        );
    }
}
