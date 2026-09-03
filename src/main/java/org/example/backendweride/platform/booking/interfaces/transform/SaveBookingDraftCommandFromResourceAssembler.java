package org.example.backendweride.platform.booking.interfaces.transform;

import org.example.backendweride.platform.booking.interfaces.resources.SaveBookingDraftResource;
import org.example.backendweride.platform.booking.domain.model.commands.SaveBookingDraftCommand;

import java.math.BigDecimal;

/**
 * Assembler class to convert SaveBookingDraftResource to SaveBookingDraftCommand.
 *
 * @summary The userId comes from the authenticated session; status, costs and payment
 *          status are computed server-side and therefore set to null / defaults.
 */
public class SaveBookingDraftCommandFromResourceAssembler {

    public static SaveBookingDraftCommand toCommand(SaveBookingDraftResource r, Long userId) {
        if (r == null) return null;

        Integer ratingScore = null;
        String ratingComment = null;

        if (r.rating() != null) {
            ratingScore = r.rating().score();
            ratingComment = r.rating().comment();
        }

        return new SaveBookingDraftCommand(
            userId,
            r.vehicleId(),
            r.startLocationId(),
            r.endLocationId(),
            r.reservedAt(),
            r.startDate(),
            r.endDate(),
            r.actualStartDate(),
            r.actualEndDate(),
            "draft",
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
