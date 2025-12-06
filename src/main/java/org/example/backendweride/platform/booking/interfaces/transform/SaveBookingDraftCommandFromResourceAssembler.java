package org.example.backendweride.platform.booking.interfaces.transform;

import org.example.backendweride.platform.booking.interfaces.resources.SaveBookingDraftResource;
import org.example.backendweride.platform.booking.domain.model.commands.SaveBookingDraftCommand;

/**
 * Assembler class to convert SaveBookingDraftResource to SaveBookingDraftCommand.
 */
public class SaveBookingDraftCommandFromResourceAssembler {

    public static SaveBookingDraftCommand toCommand(SaveBookingDraftResource r) {
        if (r == null) return null;

        Integer ratingScore = null;
        String ratingComment = null;

        if (r.rating() != null) {
            ratingScore = r.rating().score();
            ratingComment = r.rating().comment();
        }

        return new SaveBookingDraftCommand(
            r.userId(),
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

