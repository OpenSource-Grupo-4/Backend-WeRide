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
        return toCommand(r, r == null ? null : r.userId());
    }

    public static CreateBookingCommand toCommand(CreateBookingResource r, Long userId) {
        if (r == null) return null;

        Integer ratingScore = null;
        String ratingComment = null;

        if (r.rating() != null) {
            ratingScore = r.rating().score();
            ratingComment = r.rating().comment();
        }

        return new CreateBookingCommand(
            userId,
            r.vehicleId(),
            r.startLocationId(),
            r.endLocationId(),
            r.reservedAt(),
            r.startDate(),
            r.endDate(),
            r.actualStartDate(),
            r.actualEndDate(),
            r.status(),
            r.totalCost(),
            r.discount(),
            r.finalCost(),
            r.paymentMethod(),
            r.paymentStatus(),
            r.distance(),
            r.duration(),
            r.averageSpeed(),
            ratingScore,
            ratingComment
        );
    }
}
