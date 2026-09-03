package org.example.backendweride.platform.booking.interfaces.transform;

import org.example.backendweride.platform.booking.interfaces.resources.CreateBookingResource;
import org.example.backendweride.platform.booking.domain.model.commands.CreateBookingCommand;

import java.math.BigDecimal;

/**
 * Assembler class to convert CreateBookingResource to CreateBookingCommand.
 *
 * @summary This class provides a method to transform a CreateBookingResource object
 *          into a CreateBookingCommand object for processing booking creation requests.
 *          The userId is provided by the authenticated session; status, costs and
 *          payment status are computed server-side and therefore set to null.
 */
public class CreateBookingCommandFromResourceAssembler {

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
            null,
            null,
            BigDecimal.ZERO,
            null,
            r.paymentMethod() != null ? r.paymentMethod() : "card",
            "pending",
            r.distance(),
            r.duration(),
            r.averageSpeed(),
            ratingScore,
            ratingComment
        );
    }
}
