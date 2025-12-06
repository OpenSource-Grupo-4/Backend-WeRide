package org.example.backendweride.platform.booking.domain.model.queries;

/**
 * Query to get a booking by its ID.
 *
 * @summary This query retrieves a booking based on the provided booking ID.
 */
public record GetBookingByIdQuery(
    Long bookingId
) {
}
