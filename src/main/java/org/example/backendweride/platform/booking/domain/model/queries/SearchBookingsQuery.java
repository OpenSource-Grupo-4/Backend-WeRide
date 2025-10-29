package org.example.backendweride.platform.booking.domain.model.queries;

import java.time.LocalDate;

public record SearchBookingsQuery(
    String customerId,
    String vehicleId,
    String status,
    LocalDate startAtFrom,
    LocalDate startAtTo,
    int page,
    int size
) {
}
