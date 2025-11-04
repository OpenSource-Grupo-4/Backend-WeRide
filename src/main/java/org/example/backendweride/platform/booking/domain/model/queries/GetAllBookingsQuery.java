package org.example.backendweride.platform.booking.domain.model.queries;

/**
 * Query to get all bookings with pagination.
 *
 * @summary This query retrieves all bookings with specified page and size for pagination.
 */
public record GetAllBookingsQuery(
    int page,
    int size
) {
}
