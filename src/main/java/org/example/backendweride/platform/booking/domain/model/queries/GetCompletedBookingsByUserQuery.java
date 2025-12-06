package org.example.backendweride.platform.booking.domain.model.queries;

/**
 * Query to get completed bookings for a specific user.
 *
 * @summary This query retrieves all bookings with 'completed' status for a user.
 */
public record GetCompletedBookingsByUserQuery(
    Long userId
) {
}

