package org.example.backendweride.platform.booking.interfaces.transform;

import org.example.backendweride.platform.booking.domain.model.aggregates.Booking;
import org.example.backendweride.platform.booking.interfaces.resources.BookingResource;
import org.example.backendweride.platform.booking.interfaces.resources.BookingResource.RatingResource;

/**
 * Assembler class to convert Booking aggregate to BookingResource.
 *
 * @summary This class provides a method to transform a Booking aggregate object
 *          into a BookingResource object for API responses.
 */
public class BookingResourceFromEntityAssembler {

    public static BookingResource toResource(Booking booking) {
        if (booking == null) return null;

        RatingResource ratingResource = null;
        if (booking.getRating() != null) {
            ratingResource = new RatingResource(
                booking.getRating().getScore(),
                booking.getRating().getComment()
            );
        }

        return new BookingResource(
            booking.getId(),
            booking.getBookingId(),
            booking.getUserId(),
            booking.getVehicleId(),
            booking.getStartLocationId(),
            booking.getEndLocationId(),
            booking.getReservedAt(),
            booking.getStartDate(),
            booking.getEndDate(),
            booking.getActualStartDate(),
            booking.getActualEndDate(),
            booking.getStatus(),
            booking.getTotalCost(),
            booking.getDiscount(),
            booking.getFinalCost(),
            booking.getPaymentMethod(),
            booking.getPaymentStatus(),
            booking.getDistance(),
            booking.getDuration(),
            booking.getAverageSpeed(),
            ratingResource,
            booking.getIssues()
        );
    }
}
