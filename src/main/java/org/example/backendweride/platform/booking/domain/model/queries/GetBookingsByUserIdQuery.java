package org.example.backendweride.platform.booking.domain.model.queries;

/**
 * Query to get bookings by user ID.
 *
 * @summary This query retrieves all bookings for a specific user.
 */
public record GetBookingsByUserIdQuery(
    Long userId
) {
}

