package org.example.backendweride.platform.booking.domain.model.queries;

/**
 * Query to get bookings by vehicle ID with pagination.
 *
 * @summary This query retrieves bookings associated with a specific vehicle, supporting pagination through page and size parameters.
 */
public record GetBookingsByVehicleQuery(
    Long vehicleId,
    int page,
    int size
) {
}

