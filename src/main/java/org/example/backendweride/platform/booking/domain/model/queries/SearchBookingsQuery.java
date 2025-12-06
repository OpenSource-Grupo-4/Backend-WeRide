package org.example.backendweride.platform.booking.domain.model.queries;

import java.time.LocalDate;

/**
 * Query to search bookings based on various criteria with pagination.
 * @summary This query allows searching for bookings using multiple optional filters
 *          such as customer ID, vehicle ID, status, and date range, along with pagination support.
 *
 */
public record SearchBookingsQuery(
    Long customerId,
    Long vehicleId,
    String status,
    LocalDate startAtFrom,
    LocalDate startAtTo,
    int page,
    int size
) {
}
