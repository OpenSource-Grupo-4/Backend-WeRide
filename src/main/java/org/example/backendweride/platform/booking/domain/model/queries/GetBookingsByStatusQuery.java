package org.example.backendweride.platform.booking.domain.model.queries;

/**
 * Query to get bookings by status with pagination.
 *
 * @summary This query retrieves bookings with a specific status (draft, confirmed, cancelled), supporting pagination through page and size parameters.
 */
public record GetBookingsByStatusQuery(
    String status,
    int page,
    int size
) {
}

