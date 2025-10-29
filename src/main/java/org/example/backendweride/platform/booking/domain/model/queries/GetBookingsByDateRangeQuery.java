package org.example.backendweride.platform.booking.domain.model.queries;

import java.time.LocalDate;

public record GetBookingsByDateRangeQuery(
    LocalDate from,
    LocalDate to,
    int page,
    int size
) {
}
