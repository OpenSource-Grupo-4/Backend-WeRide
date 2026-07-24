package org.example.backendweride.platform.booking.domain.model.queries;

/**
 * Query to get bookings by status with pagination, scoped to the authenticated user.
 *
 * @summary This query retrieves bookings with a specific status that belong to a specific user, supporting pagination.
 */
public record GetBookingsByStatusQuery(
    Long userId,
    String status,
    int page,
    int size
) {
}
