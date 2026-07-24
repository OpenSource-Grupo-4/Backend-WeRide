package org.example.backendweride.platform.booking.domain.model.queries;

/**
 * Query to get bookings by vehicle ID with pagination, scoped to the authenticated user.
 *
 * @summary This query retrieves bookings for a specific vehicle that belong to a specific user, supporting pagination.
 */
public record GetBookingsByVehicleQuery(
    Long userId,
    Long vehicleId,
    int page,
    int size
) {
}
