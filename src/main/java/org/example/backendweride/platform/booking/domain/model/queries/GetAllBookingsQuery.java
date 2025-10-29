package org.example.backendweride.platform.booking.domain.model.queries;

public record GetAllBookingsQuery(
    int page,
    int size
) {
}
