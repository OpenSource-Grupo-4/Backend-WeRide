package org.example.backendweride.platform.booking.domain.model.queries;

/**
 * Query to get pending bookings for a specific user.
 *
 * @summary This query retrieves all bookings with 'pending' or 'confirmed' status for a user.
 */
public record GetPendingBookingsByUserQuery(
    Long userId
) {
}

